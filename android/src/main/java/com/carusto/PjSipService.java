package com.carusto;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.*;
import android.os.Process;
import android.util.Log;
import org.pjsip.pjsua2.*;

import java.util.*;

public class PjSipService extends Service {

    private static String TAG = "PjSipService";

    private HandlerThread mWorkerThread;

    private Handler mHandler;

    private Endpoint mEndpoint;

    private PjSipLogWriter mLogWriter;

    private PjSipBroadcastEmiter mEmitter;

    private List<PjSipAccount> mAccounts = new ArrayList<>();

    private List<PjSipCall> mCalls = new ArrayList<>();

    private List<Object> mTrash = new LinkedList<>();

    private AudioManager mAudioManager;

    public PjSipBroadcastEmiter getEmitter() {
        return mEmitter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        super.onCreate();

        mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
        mWorkerThread.setPriority(Thread.MAX_PRIORITY);
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());

        mEmitter = new PjSipBroadcastEmiter(this);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run(){
//                job(new Runnable() {
//                    @Override
//                    public void run() {
//                        Log.d(TAG, "service tick");
//                    }
//                });
//            }
//        }, 0, 1000);

        job(new Runnable() {
            @Override
            public void run() {
                load();

//                Logger.debug(TAG, "Creating SipService with priority: " + Thread.currentThread().getPriority());
//
//                loadNativeLibraries();
//
//                mRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(SipService.this, RingtoneManager.TYPE_RINGTONE);

//                mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//
//                mBroadcastEmitter = new BroadcastEventEmitter(SipService.this);
//                loadConfiguredAccounts();
//                addAllConfiguredAccounts();
//
//                Logger.debug(TAG, "SipService created!");
            }
        });
    }

    private void load() {
        // Load native libraries
        try {
            System.loadLibrary("openh264");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "Error while loading OpenH264 native library", error);
            throw new RuntimeException(error);
        }

        try {
            System.loadLibrary("yuv");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "Error while loading libyuv native library", error);
            throw new RuntimeException(error);
        }

        try {
            System.loadLibrary("pjsua2");
        } catch (UnsatisfiedLinkError error) {
            Log.e(TAG, "Error while loading PJSIP pjsua2 native library", error);
            throw new RuntimeException(error);
        }

        // Start stack
        try {
            mEndpoint = new Endpoint();
            mEndpoint.libCreate();
            mEndpoint.libRegisterThread(Thread.currentThread().getName());

            EpConfig epConfig = new EpConfig();

            epConfig.getLogConfig().setLevel(4);
            epConfig.getLogConfig().setConsoleLevel(4);

            mLogWriter = new PjSipLogWriter();
            epConfig.getLogConfig().setWriter(mLogWriter);

            // epConfig.getUaConfig().setUserAgent("");
            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(8000);
            epConfig.getMedConfig().setQuality(4);
            epConfig.getMedConfig().setEcOptions(1);
            epConfig.getMedConfig().setEcTailLen(200);
            epConfig.getMedConfig().setThreadCnt(2);
            mEndpoint.libInit(epConfig);

            mTrash.add(epConfig);

            mEndpoint.libStart();
        } catch (Exception e) {
            Log.e(TAG, "Error while starting PJSIP", e);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        job(new Runnable() {
            @Override
            public void run() {
                handle(intent);
            }
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mWorkerThread.quitSafely();
        super.onDestroy();
    }

    private void job(Runnable job) {
        mHandler.post(job);
    }

    protected synchronized AudDevManager getAudDevManager() {
        return mEndpoint.audDevManager();
    }

    public void evict(final PjSipAccount account) {
        if (mHandler.getLooper().getThread() != Thread.currentThread()) {
            job(new Runnable() {
                @Override
                public void run() {
                    evict(account);
                }
            });
            return;
        }

        // Remove link to account
        mAccounts.remove(account);

        // Remove transport
        try {
            mEndpoint.transportClose(account.getTransportId());
        } catch (Exception e) {
            Log.w(TAG, "Failed to close transport for account", e);
        }

        // Remove account in PjSip
        account.delete();

    }

    public void evict(final PjSipCall call) {
        if (mHandler.getLooper().getThread() != Thread.currentThread()) {
            job(new Runnable() {
                @Override
                public void run() {
                    evict(call);
                }
            });
            return;
        }

        // Remove link to call
        mCalls.remove(call);

        // Remove call in PjSip
        call.delete();
    }


    private void handle(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        switch (intent.getAction()) {
            // General actions
            case PjActions.ACTION_START:
                handleStart(intent);
                break;

            // Account actions
            case PjActions.ACTION_CREATE_ACCOUNT:
                handleAccountCreate(intent);
                break;
            case PjActions.ACTION_DELETE_ACCOUNT:
                handleAccountDelete(intent);
                break;

            // Call actions
            case PjActions.ACTION_MAKE_CALL:
                handleCallMake(intent);
                break;
            case PjActions.ACTION_HANGUP_CALL:
                handleCallHangup(intent);
                break;
            case PjActions.ACTION_ANSWER_CALL:
                handleCallAnswer(intent);
                break;
            case PjActions.ACTION_HOLD_CALL:
                handleCallSetOnHold(intent);
                break;
            case PjActions.ACTION_UNHOLD_CALL:
                handleCallReleaseFromHold(intent);
                break;
            case PjActions.ACTION_XFER_CALL:
                // TODO: handleCallXFer(intent);
                break;
            case PjActions.ACTION_DTMF_CALL:
                // TODO: handleCallDtmf(intent);
                break;
        }
    }

    /**
     * @param intent
     */
    private void handleStart(Intent intent) {
        mEmitter.fireStarted(intent, mAccounts, mCalls);
    }

    /**
     * @param intent
     */
    private void handleAccountCreate(Intent intent) {
        try {
            String username = intent.getStringExtra("username");
            String password = intent.getStringExtra("password");
            String host = intent.getStringExtra("host");
            String realm = intent.getStringExtra("realm");
            String port = intent.getStringExtra("port");
            String transport = intent.getStringExtra("transport");
            String uri = port != null && !port.isEmpty() ? host + ":" + port : host;

            // Create transport
            TransportConfig transportConfig = new TransportConfig();
            transportConfig.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

            pjsip_transport_type_e transportType = pjsip_transport_type_e.PJSIP_TRANSPORT_UDP;

            if (transport != null && !transport.isEmpty() && !transport.equals("TCP")) {
                switch (transport) {
                    case "UDP":
                        transportType = pjsip_transport_type_e.PJSIP_TRANSPORT_TCP;
                        break;
                    case "TLS":
                        transportType = pjsip_transport_type_e.PJSIP_TRANSPORT_TLS;
                        break;
                    default:
                        Log.w(TAG, "Illegal \""+ transport +"\" transport (possible values are UDP, TCP or TLS) use TCP instead");
                        break;
                }
            }

            int transportId = mEndpoint.transportCreate(transportType, transportConfig);

            // Create account
            AccountConfig cfg = new AccountConfig();
            cfg.setIdUri("sip:"+ username + "@" + realm);
            cfg.getRegConfig().setRegistrarUri("sip:" + uri);
            AuthCredInfo cred = new AuthCredInfo("Digest", realm, username, 0, password);
            cfg.getSipConfig().getAuthCreds().add(cred);
            cfg.getSipConfig().setTransportId(transportId);
            cfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            cfg.getRegConfig().setRegisterOnAdd(true);
            cfg.getVideoConfig().setAutoTransmitOutgoing(true);

            // TODO: Pass username, password, host, realm into Account object for further retrieval
            PjSipAccount account = new PjSipAccount(this, transportId);
            account.create(cfg);

            mTrash.add(cfg);
            mTrash.add(cred);
            mTrash.add(transportConfig);

            mAccounts.add(account);
            mEmitter.fireAccountCreated(intent, account);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleAccountDelete(Intent intent) {
        try {
            int accountId = intent.getIntExtra("account_id", -1);
            PjSipAccount account = null;

            for (PjSipAccount a : mAccounts) {
                if (a.getId() == accountId) {
                    account = a;
                    break;
                }
            }

            if (account == null) {
                throw new Exception("Account with \""+ accountId +"\" id not found");
            }

            evict(account);

            // -----
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallMake(Intent intent) {
        try {
            Log.d(TAG, "handleCallMake start");

            int accountId = intent.getIntExtra("account_id", -1);
            String destination = intent.getStringExtra("destination");

            // -----
            PjSipAccount account = findAccount(accountId);

            // -----
            CallOpParam prm = new CallOpParam(true);
            // TODO: Allow to send also headers and other information

            // -----
            PjSipCall call = new PjSipCall(account);
            call.makeCall(destination, prm);

            mCalls.add(call);
            mEmitter.fireCallCreated(intent, call);

            Log.d(TAG, "handleCallMake end");
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallHangup(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.hangup(new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallAnswer(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.answer(new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallSetOnHold(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.putOnHold();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallReleaseFromHold(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.releaseFromHold();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }



    private PjSipAccount findAccount(int id) throws Exception {
        for (PjSipAccount account : mAccounts) {
            if (account.getId() == id) {
                return account;
            }
        }

        throw new Exception("Account with specified \""+ id +"\" id not found");
    }

    private PjSipCall findCall(int id) throws Exception {
        for (PjSipCall call : mCalls) {
            if (call.getId() == id) {
                return call;
            }
        }

        throw new Exception("Call with specified \""+ id +"\" id not found");
    }
}
