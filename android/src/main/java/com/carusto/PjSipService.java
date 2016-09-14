package com.carusto;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.AudioManager;
import android.os.*;
import android.os.Process;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.pjsip.pjsua2.*;

import java.util.*;

public class PjSipService extends Service {

    private static String TAG = "PjSipService";

    private HandlerThread mWorkerThread;

    private Handler mHandler;

    private boolean mAppHidden;

    private Endpoint mEndpoint;

    private PjSipLogWriter mLogWriter;

    private PjSipBroadcastEmiter mEmitter;

    private List<PjSipAccount> mAccounts = new ArrayList<>();

    private List<PjSipCall> mCalls = new ArrayList<>();

    private List<Object> mTrash = new LinkedList<>();

    private AudioManager mAudioManager;

    private boolean mGSMIdle;

    private TelephonyManager mTelephonyManager;

    private BroadcastReceiver mPhoneStateChangedReceiver = new PhoneStateChangedReceiver();

    public PjSipBroadcastEmiter getEmitter() {
        return mEmitter;
    }

    protected static final int NOTIFICATION = 1;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        // TODO: Use PowerManager to lock while in call.
        // TODO: Use WifiManager to lock while active account exists.
        // TODO: Use TelephonyManager to check whether GSM call is available (we should put all calls on hold and don't allow to initiate outgoing calls when received GSM call, also stop ringing if incoming call available)
        // TODO: Use mAudioManager.setMode(MODE_IN_CALL);
        // TODO: Ability to adjust volume (starting from ICS, volume must be adjusted by the application, at least for STREAM_VOICE_CALL volume stream)
        // TODO: Ability to set ringing sound
        // TODO: Clean up mTrash once call or account not needed

        super.onCreate();

        mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
        mWorkerThread.setPriority(Thread.MAX_PRIORITY);
        mWorkerThread.start();
        mHandler = new Handler(mWorkerThread.getLooper());

        mEmitter = new PjSipBroadcastEmiter(this);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mGSMIdle = mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;

        IntentFilter phoneStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(mPhoneStateChangedReceiver, phoneStateFilter);

        job(new Runnable() {
            @Override
            public void run() {
                load();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }
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
            case PjActions.ACTION_MUTE_CALL:
                handleCallMute(intent);
                break;
            case PjActions.ACTION_UNMUTE_CALL:
                handleCallUnMute(intent);
                break;
            case PjActions.ACTION_USE_SPEAKER_CALL:
                handleCallUseSpeaker(intent);
                break;
            case PjActions.ACTION_USE_EARPIECE_CALL:
                handleCallUseEarpiece(intent);
                break;
            case PjActions.ACTION_XFER_CALL:
                handleCallXFer(intent);
                break;
            case PjActions.ACTION_REDIRECT_CALL:
                handleCallRedirect(intent);
                break;
            case PjActions.ACTION_DTMF_CALL:
                handleCallDtmf(intent);
                break;
            case PjActions.ACTION_START_FOREGROUND:
                handleStartForeground(intent);
                break;
            case PjActions.ACTION_STOP_FOREGROUND:
                handleStopForeground(intent);
                break;

            // Extra actions
            case PjActions.EVENT_APP_VISIBLE:
                mAppHidden = false;
                break;
            case PjActions.EVENT_APP_HIDDEN:
                mAppHidden = true;
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
            String name = intent.getStringExtra("name");
            String username = intent.getStringExtra("username");
            String domain = intent.getStringExtra("domain");
            String password = intent.getStringExtra("password");
            String proxy = intent.getStringExtra("proxy");
            String transport = intent.getStringExtra("transport");
            String regServer = intent.getStringExtra("regServer");
            Integer regTimeout = null;

            if (intent.hasExtra("regTimeout")) {
                regTimeout = intent.getIntExtra("regTimeout", -1);
            }

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

            AuthCredInfo cred = new AuthCredInfo(
                "Digest",
                regServer != null && regServer.length() > 0 ? regServer : "*" ,
                username,
                0,
                password
            );

            String idUri = name + " <sip:"+ username +"@"+ domain +">";
            String regUri = "sip:"+ domain;

            AccountConfig cfg = new AccountConfig();
            cfg.setIdUri(idUri);
            cfg.getRegConfig().setRegistrarUri(regUri);
            cfg.getSipConfig().getAuthCreds().add(cred);
            cfg.getSipConfig().setTransportId(transportId);
            cfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
            cfg.getRegConfig().setRegisterOnAdd(true);
            cfg.getVideoConfig().setAutoTransmitOutgoing(true);

            if (proxy != null && proxy.length() > 0) {
                StringVector v = new StringVector();
                v.add(proxy);
                cfg.getSipConfig().setProxies(v);
            }

            PjSipAccount account = new PjSipAccount(this, transportId, name, username, domain, password, proxy, transport, regServer, regTimeout);
            account.create(cfg);

            mTrash.add(cfg);
            mTrash.add(cred);
            mTrash.add(transportConfig);

            mAccounts.add(account);
            mEmitter.fireAccountCreated(intent, account);

            // -----
            handleForeground();

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

            // -----
            handleForeground();
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    public void handleCallReceived(PjSipCall call) {
        // Automatically decline incoming call when user uses GSM
        if (!mGSMIdle) {
            try {
                call.hangup(new CallOpParam(true));
            } catch (Exception e) {
                Log.d(TAG, "Failed to decline incoming call when user uses GSM", e);
            }

            return;
        }

        // Automatically start application when incoming call received.
        if (mAppHidden) {
            try {
                String ns = getApplicationContext().getPackageName();
                String cls = ns + ".MainActivity";

                Intent intent = new Intent(getApplicationContext(), Class.forName(cls));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.EXTRA_DOCK_STATE_CAR);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);

                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open application on received call", e);
            }
        }

        // -----
        mCalls.add(call);
        mEmitter.fireCallReceivedEvent(call);
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
            CallOpParam prm = new CallOpParam();
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_OK);
            call.answer(prm);

            // Automatically put other calls on hold.
            doPauseParallelCalls(call);

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
            call.hold();

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
            call.unhold();

            // Automatically put other calls on hold.
            doPauseParallelCalls(call);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallMute(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.mute();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallUnMute(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.unmute();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    // TODO: When active calls ends, we should reset speaker.
    private void handleCallUseSpeaker(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.useSpeaker();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    // TODO: Log each action for debug proposal
    private void handleCallUseEarpiece(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            call.useEarpiece();

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallXFer(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String destination = intent.getStringExtra("destination");

            // -----
            PjSipCall call = findCall(callId);
            call.xfer(destination, new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallRedirect(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String destination = intent.getStringExtra("destination");

            // -----
            PjSipCall call = findCall(callId);
            call.redirect(destination);

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallDtmf(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            String digits = intent.getStringExtra("digits");

            // -----
            PjSipCall call = findCall(callId);
            call.dialDtmf(digits);

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

    private void handleStartForeground(Intent intent) {
        try {
            String title = intent.getStringExtra("title");
            String text = intent.getStringExtra("text");
            String info = intent.getStringExtra("info");
            String ticker = intent.getStringExtra("ticker");
            String smallIcon = intent.getStringExtra("smallIcon");
            String largeIcon = intent.getStringExtra("largeIcon");

            startForeground(NOTIFICATION, buildNotification(title, text, info, ticker, smallIcon, largeIcon));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleStopForeground(Intent intent) {
        try {
            stopForeground(true);
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleForeground() throws ClassNotFoundException {
        // TODO: Add ability to handle foreground state automatically.

        /**
        if (mAccounts.size() > 0) {
            startForeground(NOTIFICATION, buildNotification());
        } else {
            stopForeground(true);
        }
        **/
    }

    private Notification buildNotification(String title, String text, String info, String ticker, String smallIcon, String largeIcon)
            throws ClassNotFoundException {
        String foregroundIntentName = getApplicationContext().getPackageName() + ".MainActivity";
        Class<?> foregroundIntentCls = Class.forName(foregroundIntentName);

        Intent foregroundIntent = new Intent(this, foregroundIntentCls);
        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        if (title != null && title.length() > 0) {
            b.setContentTitle(title);
        }
        if (text != null && text.length() > 0) {
            b.setContentText(text);
        }
        if (info != null && info.length() > 0) {
            b.setContentInfo(info);
        }
        if (ticker != null && ticker.length() > 0) {
            b.setTicker(ticker);
        }

        Resources resources = getApplicationContext().getResources();

        if (smallIcon == null) {
            smallIcon = "ic_launcher";
        }
        int smallIconId = resources.getIdentifier(smallIcon, "drawable", getApplicationContext().getPackageName());

        if (smallIconId > 0) {
            b.setSmallIcon(smallIconId);
        }

        // TODO: Add ability to use largeIcon

        return b.setContentIntent(pendIntent).setWhen(0).build();
    }

    /**
     * Pauses active calls once user answer to incoming calls.
     */
    private void doPauseParallelCalls(PjSipCall activeCall) {
        for (PjSipCall call : mCalls) {
            if (activeCall.getId() == call.getId()) {
                continue;
            }

            try {
                call.hold();
            } catch (Exception e) {
                Log.d(TAG, "Failed to put call on hold", e);
            }
        }
    }

    /**
     * Pauses all calls, used when received GSM call.
     */
    private void doPauseAllCalls() {
        for (PjSipCall call : mCalls) {
            try {
                call.hold();
            } catch (Exception e) {
                Log.d(TAG, "Failed to put call on hold", e);
            }
        }
    }


    protected class PhoneStateChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(extraState) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(extraState)) {
                mGSMIdle = false;

                doPauseAllCalls();
                // stopRinging();

                Log.d(TAG, "GSM call received, pause all SIP calls and do not accept incoming SIP calls.");

            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(extraState)) {
                Log.d(TAG, "GSM call released, allow to accept incoming calls.");
                mGSMIdle = true;
            }
        }
    }
}
