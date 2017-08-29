package com.carusto.ReactNativePjSip;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.Vibrator;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.carusto.ReactNativePjSip.configuration.AccountConfiguration;
import com.carusto.ReactNativePjSip.configuration.ConfigurationUtils;
import com.carusto.ReactNativePjSip.configuration.NetworkConfiguration;
import com.carusto.ReactNativePjSip.configuration.ServiceConfiguration;
import com.carusto.ReactNativePjSip.configuration.ServiceNotificationConfiguration;
import com.carusto.ReactNativePjSip.utils.ArgumentUtils;

import org.json.JSONObject;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AudDevManager;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.CallOpParam;
import org.pjsip.pjsua2.Endpoint;
import org.pjsip.pjsua2.EpConfig;
import org.pjsip.pjsua2.OnCallStateParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.SipHeader;
import org.pjsip.pjsua2.SipHeaderVector;
import org.pjsip.pjsua2.StringVector;
import org.pjsip.pjsua2.TransportConfig;
import org.pjsip.pjsua2.pj_qos_type;
import org.pjsip.pjsua2.pjsip_inv_state;
import org.pjsip.pjsua2.pjsip_status_code;
import org.pjsip.pjsua2.pjsip_transport_type_e;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PjSipService extends Service implements SensorEventListener {

    private static String TAG = "PjSipService";

    private static final float PROXIMITY_THRESHOLD = 5.0f;

    private static final int NOTIFICATION_ACTIVE_REGISTRATION = 1;

    private static final int NOTIFICATION_ACTIVE_CALL = 2;

    private boolean mInitialized;

    private HandlerThread mWorkerThread;

    private Handler mHandler;

    private boolean mAppHidden;

    private Endpoint mEndpoint;

    private int mUdpTransportId;

    private int mTcpTransportId;

    private int mTlsTransportId;

    private ServiceConfiguration mServiceConfiguration = ServiceConfiguration.defaultConfiguration();

    private NetworkConfiguration mNetworkConfiguration = NetworkConfiguration.defaultConfiguration();

    private PjSipLogWriter mLogWriter;

    private PjSipBroadcastEmiter mEmitter;

    private List<PjSipAccount> mAccounts = new ArrayList<>();

    private List<PjSipCall> mCalls = new ArrayList<>();

    // In order to ensure that GC will not destroy objects that are used in PJSIP
    // Also there is limitation of pjsip that thread should be registered first before working with library
    // (but we couldn't register GC thread in pjsip)
    private List<Object> mTrash = new LinkedList<>();

    private NotificationManager mNotificationManager;

    private AudioManager mAudioManager;

    private HashSet<Integer> mCallsRingerLocks = new HashSet<>();

    private MediaPlayer mRingerPlayer;

    private boolean mUseSpeaker = false;

    private Vibrator mVibrator;

    private PowerManager mPowerManager;

    private PowerManager.WakeLock mIncallWakeLock;

    private TelephonyManager mTelephonyManager;

    private WifiManager mWifiManager;

    private ConnectivityManager mConnectivityManager;

    private boolean mConnectivityAvailable;

    private WifiManager.WifiLock mWifiLock;

    private boolean mGSMIdle;

    private SensorManager mSensorManager;

    private Sensor mProximitySensor;

    private boolean mProximitySensorTracked = false;

    private boolean mProximityFirstRun = true;

    private BroadcastReceiver mPhoneStateChangedReceiver = new PhoneStateChangedReceiver();

    public PjSipBroadcastEmiter getEmitter() {
        return mEmitter;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

        // ServiceConfiguration configuration = PjSipSharedPreferences.getServiceSettings(getBaseContext());

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

            if (mServiceConfiguration.isUserAgentNotEmpty()) {
                epConfig.getUaConfig().setUserAgent(mServiceConfiguration.getUserAgent());
            } else {
                epConfig.getUaConfig().setUserAgent("React Native PjSip ("+ mEndpoint.libVersion().getFull() +")");
            }

            epConfig.getMedConfig().setHasIoqueue(true);
            epConfig.getMedConfig().setClockRate(8000);
            epConfig.getMedConfig().setQuality(4);
            epConfig.getMedConfig().setEcOptions(1);
            epConfig.getMedConfig().setEcTailLen(200);
            epConfig.getMedConfig().setThreadCnt(2);
            mEndpoint.libInit(epConfig);

            mTrash.add(epConfig);

            // Add transports
            {
                TransportConfig transportConfig = new TransportConfig();
                transportConfig.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
                mUdpTransportId = mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, transportConfig);
                mTrash.add(transportConfig);
            }
            {
                TransportConfig transportConfig = new TransportConfig();
                transportConfig.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
                mTcpTransportId = mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, transportConfig);
                mTrash.add(transportConfig);
            }
            {
                TransportConfig transportConfig = new TransportConfig();
                transportConfig.setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);
                mTlsTransportId = mEndpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS, transportConfig);
                mTrash.add(transportConfig);
            }

            mEndpoint.libStart();
        } catch (Exception e) {
            Log.e(TAG, "Error while starting PJSIP", e);
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (!mInitialized) {
            if (intent.getAction().equals(PjActions.EVENT_APP_VISIBLE) ||
                intent.getAction().equals(PjActions.EVENT_APP_HIDDEN) ||
                intent.getAction().equals(PjActions.EVENT_APP_DESTROY) ||
                intent.getAction().equals(PjActions.EVENT_CONNECTIVITY_CHANGED)) {
                return START_REDELIVER_INTENT;
            }

            if (intent.hasExtra("service")) {
                mServiceConfiguration = ServiceConfiguration.fromMap((Map) intent.getSerializableExtra("service"));
            }
            if (intent.hasExtra("network")) {
                mNetworkConfiguration = NetworkConfiguration.fromMap((Map) intent.getSerializableExtra("network"));
            }

            mWorkerThread = new HandlerThread(getClass().getSimpleName(), Process.THREAD_PRIORITY_FOREGROUND);
            mWorkerThread.setPriority(Thread.MAX_PRIORITY);
            mWorkerThread.start();
            mHandler = new Handler(mWorkerThread.getLooper());
            mEmitter = new PjSipBroadcastEmiter(this);
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
            mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            mWifiLock = mWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, this.getPackageName()+"-wifi-call-lock");
            mWifiLock.setReferenceCounted(false);
            mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            mConnectivityAvailable = mNetworkConfiguration.isMatches(mConnectivityManager.getActiveNetworkInfo());
            mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            mGSMIdle = mTelephonyManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            IntentFilter phoneStateFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            registerReceiver(mPhoneStateChangedReceiver, phoneStateFilter);

            mInitialized = true;

            job(new Runnable() {
                @Override
                public void run() {
                    load();
                }
            });
        } else if (intent.getAction().equals(PjActions.ACTION_START)) {
            // Modify existing configuration if it changes during application reload.
            if (intent.hasExtra("service")) {
                ServiceConfiguration newServiceConfiguration = ServiceConfiguration.fromMap((Map) intent.getSerializableExtra("service"));
                if (!newServiceConfiguration.equals(mServiceConfiguration)) {
                    updateServiceConfiguration(newServiceConfiguration);
                }
            }
            if (intent.hasExtra("network")) {
                NetworkConfiguration newNetworkConfiguration = NetworkConfiguration.fromMap((Map) intent.getSerializableExtra("network"));
                if (!newNetworkConfiguration.equals(mNetworkConfiguration)) {
                    updateNetworkConfiguration(newNetworkConfiguration);
                }
            }
        }

        job(new Runnable() {
            @Override
            public void run() {
                handle(intent);
            }
        });

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mWorkerThread.quitSafely();
        }

        try {
            if (mEndpoint != null) {
                mEndpoint.libDestroy();
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to destroy PjSip library", e);
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

        // Remove call notification
        if (mCalls.size() == 0) {
            mNotificationManager.cancel(NOTIFICATION_ACTIVE_CALL);
        }

        // Remove call in PjSip
        call.delete();
    }


    private void handle(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        Log.d(TAG, "Handle \""+ intent.getAction() +"\" action ("+ ArgumentUtils.dumpIntentExtraParameters(intent) +")");

        switch (intent.getAction()) {
            // General actions
            case PjActions.ACTION_START:
                handleStart(intent);
                break;

            // Account actions
            case PjActions.ACTION_CREATE_ACCOUNT:
                handleAccountCreate(intent);
                break;
            case PjActions.ACTION_REGISTER_ACCOUNT:
                handleAccountRegister(intent);
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
            case PjActions.ACTION_DECLINE_CALL:
                handleCallDecline(intent);
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
            case PjActions.ACTION_XFER_REPLACES_CALL:
                handleCallXFerReplaces(intent);
                break;
            case PjActions.ACTION_REDIRECT_CALL:
                handleCallRedirect(intent);
                break;
            case PjActions.ACTION_DTMF_CALL:
                handleCallDtmf(intent);
                break;

            // Configuration actions
            case PjActions.ACTION_SET_NETWORK_CONFIGURATION:
                handleSetNetworkConfiguration(intent);
                break;
            case PjActions.ACTION_SET_SERVICE_CONFIGURATION:
                handleSetServiceConfiguration(intent);
                break;

            // Extra actions
            case PjActions.EVENT_CONNECTIVITY_CHANGED:
                handleConnectivityChangedEvent();
                break;
            case PjActions.EVENT_APP_VISIBLE:
                mAppHidden = false;
                break;
            case PjActions.EVENT_APP_HIDDEN:
                mAppHidden = true;
                break;
            case PjActions.EVENT_APP_DESTROY:
                mAppHidden = true;
                // TODO: Add this event and stop service in case no connectivity or empty accounts or foreground is false

                break;
        }
    }

    /**
     * @param intent
     */
    private void handleStart(Intent intent) {
        // Format settings
        JSONObject settings = ConfigurationUtils.getSettingsAsJson(mServiceConfiguration, mNetworkConfiguration);

        mEmitter.fireStarted(intent, mAccounts, mCalls, settings, mConnectivityAvailable);
    }

    private void handleSetNetworkConfiguration(Intent intent) {
        try {
            updateNetworkConfiguration(NetworkConfiguration.fromIntent(intent));

            // Emmit response
            mEmitter.fireIntentHandled(intent, mNetworkConfiguration.toJson());

            // Emmit connectivity event
            mEmitter.fireConnectivityChanged(mConnectivityAvailable);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void updateNetworkConfiguration(NetworkConfiguration configuration) {
        Log.d(TAG, "updateNetworkConfiguration: do update!!!");

        mNetworkConfiguration = configuration;
        boolean matches = mNetworkConfiguration.isMatches(mConnectivityManager.getActiveNetworkInfo());

        if (mConnectivityAvailable != matches) {
            mConnectivityAvailable = matches;

            if (matches) {
                handleConnectivityAvailable();
            } else {
                handleConnectivityUnavailable();
            }
        }
    }

    private void handleSetServiceConfiguration(Intent intent) {
        try {
            updateServiceConfiguration(ServiceConfiguration.fromIntent(intent));

            // Emmit response
            JSONObject response = ConfigurationUtils.getSettingsAsJson(mServiceConfiguration, mNetworkConfiguration);
            mEmitter.fireIntentHandled(intent, response);

        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void updateServiceConfiguration(ServiceConfiguration configuration) {
        Log.d(TAG, "updateServiceConfiguration: do update!!!");

        mServiceConfiguration = configuration;

        // Add or remove foreground notification
        if (mConnectivityAvailable && mServiceConfiguration.getAccountNotificationConfiguration().isEnabled()) {
            doStartForeground();
        } else {
            doStopForeground();
        }
    }

    private void handleConnectivityChangedEvent() {
        NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
        final boolean matches = mNetworkConfiguration.isMatches(info);

        if (mConnectivityAvailable != matches) {
            mConnectivityAvailable = matches;

            job(new Runnable() {
                @Override
                public void run() {
                    if (matches) {
                        handleConnectivityAvailable();
                    } else {
                        handleConnectivityUnavailable();
                    }
                }
            });

            // Emmit event to React application
            mEmitter.fireConnectivityChanged(mConnectivityAvailable);
        }
    }

    /**
     * Fires when application can register.
     * Also service should be started in foreground to persist connection to server (only if more then one account).
     */
    private void handleConnectivityAvailable() {
        if (mAccounts.size() == 0) {
            return;
        }

        // Force register on server
        for (PjSipAccount account : mAccounts) {
            try {
                account.setRegistration(true);
            } catch (Exception e) {
                Log.w(TAG, "Failed to register on server", e);
            }
        }

        // Start foreground
        if (mServiceConfiguration.getAccountNotificationConfiguration().isEnabled()) {
            doStartForeground();
        }
    }

    /**
     * Fires when application should disable any active registration.
     * Also service should be stopped in foreground, until connection will be available.
     */
    private void handleConnectivityUnavailable() {
        // Stop foreground
        doStopForeground();

        // Try to send unregister event
        for (PjSipAccount account : mAccounts) {
            try {
                account.setRegistration(false);
            } catch (Exception e) {

            }
        }
    }

    /**
     * @param intent
     */
    private void handleAccountCreate(Intent intent) {
        try {
            AccountConfiguration accountConfiguration = AccountConfiguration.fromIntent(intent);
            PjSipAccount account = doAccountCreate(accountConfiguration);

            // Emmit response
            mEmitter.fireAccountCreated(intent, account);

            // Start foreground
            if (mConnectivityAvailable && mServiceConfiguration.getAccountNotificationConfiguration().isEnabled()) {
                doStartForeground();
            }
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    /**
     * @param intent
     */
    private void handleAccountRegister(Intent intent) {
        try {
            int accountId = intent.getIntExtra("account_id", -1);
            boolean renew = intent.getBooleanExtra("renew", false);
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

            account.register(renew);

            // -----
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private PjSipAccount doAccountCreate(AccountConfiguration configuration) throws Exception {
        AccountConfig cfg = new AccountConfig();

        // General settings

        AuthCredInfo cred = new AuthCredInfo(
            "Digest",
            configuration.getNomalizedRegServer(),
            configuration.getUsername(),
            0,
            configuration.getPassword()
        );

        String idUri = configuration.getIdUri();
        String regUri = configuration.getRegUri();

        cfg.setIdUri(idUri);
        cfg.getRegConfig().setRegistrarUri(regUri);
        cfg.getRegConfig().setRegisterOnAdd(mConnectivityAvailable && configuration.isRegOnAdd());
        cfg.getSipConfig().getAuthCreds().add(cred);

        // Registration settings

        if (configuration.getContactParams() != null) {
            cfg.getSipConfig().setContactParams(configuration.getContactParams());
        }
        if (configuration.getContactUriParams() != null) {
            cfg.getSipConfig().setContactUriParams(configuration.getContactUriParams());
        }
        if (configuration.getRegContactParams() != null) {
            Log.w(TAG, "Property regContactParams are not supported on android, use contactParams instead");
        }

        if (configuration.getRegHeaders() != null && configuration.getRegHeaders().size() > 0) {
            SipHeaderVector headers = new SipHeaderVector();

            for (Map.Entry<String, String> entry : configuration.getRegHeaders().entrySet()) {
                SipHeader hdr = new SipHeader();
                hdr.setHName(entry.getKey());
                hdr.setHValue(entry.getValue());
                headers.add(hdr);
            }

            cfg.getRegConfig().setHeaders(headers);
        }

        // Transport settings
        int transportId = mTcpTransportId;

        if (configuration.isTransportNotEmpty()) {
            switch (configuration.getTransport()) {
                case "UDP":
                    transportId = mUdpTransportId;
                    break;
                case "TLS":
                    transportId = mTlsTransportId;
                    break;
                default:
                    Log.w(TAG, "Illegal \""+ configuration.getTransport() +"\" transport (possible values are UDP, TCP or TLS) use TCP instead");
                    break;
            }
        }

        cfg.getSipConfig().setTransportId(transportId);

        if (configuration.isProxyNotEmpty()) {
            StringVector v = new StringVector();
            v.add(configuration.getProxy());
            cfg.getSipConfig().setProxies(v);
        }

        cfg.getMediaConfig().getTransportConfig().setQosType(pj_qos_type.PJ_QOS_TYPE_VOICE);

        // -----

        PjSipAccount account = new PjSipAccount(this, transportId, configuration);
        account.create(cfg);

        mTrash.add(cfg);
        mTrash.add(cred);

        mAccounts.add(account);

        return account;
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

            // Stop foreground
            if (mAccounts.size() == 0) {
                doStopForeground();
            }

            // -----
            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallMake(Intent intent) {

        try {
            int accountId = intent.getIntExtra("account_id", -1);
            PjSipAccount account = findAccount(accountId);
            String destination = intent.getStringExtra("destination");

            // -----
            CallOpParam prm = new CallOpParam(true);
            PjSipCall call = new PjSipCall(account);
            call.makeCall(destination, prm);
            prm.delete();

            // Automatically put other calls on hold.
            doPauseParallelCalls(call);

            // Show call notification
            if (mServiceConfiguration.getCallNotificationConfiguration().isEnabled()) {
                Notification notification = buildCallNotification(mServiceConfiguration.getCallNotificationConfiguration(), account, call);
                mNotificationManager.notify(NOTIFICATION_ACTIVE_CALL, notification);
            }

            mCalls.add(call);

            mEmitter.fireIntentHandled(intent, call.toJson());
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallHangup(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            PjSipCall call = findCall(callId);
            call.hangup(new CallOpParam(true));

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallDecline(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            CallOpParam prm = new CallOpParam(true);
            prm.setStatusCode(pjsip_status_code.PJSIP_SC_DECLINE);
            call.hangup(prm);
            prm.delete();

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

    private void handleCallUseSpeaker(Intent intent) {
        try {
            mAudioManager.setSpeakerphoneOn(true);
            mUseSpeaker = true;

            for (PjSipCall call : mCalls) {
                emmitCallUpdated(call);
            }

            mEmitter.fireIntentHandled(intent);
        } catch (Exception e) {
            mEmitter.fireIntentHandled(intent, e);
        }
    }

    private void handleCallUseEarpiece(Intent intent) {
        try {
            mAudioManager.setSpeakerphoneOn(false);
            mUseSpeaker = false;

            for (PjSipCall call : mCalls) {
                emmitCallUpdated(call);
            }

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

    private void handleCallXFerReplaces(Intent intent) {
        try {
            int callId = intent.getIntExtra("call_id", -1);
            int destinationCallId = intent.getIntExtra("dest_call_id", -1);

            // -----
            PjSipCall call = findCall(callId);
            PjSipCall destinationCall = findCall(destinationCallId);
            call.xferReplaces(destinationCall, new CallOpParam(true));

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

    private void doStartForeground() {
        ServiceNotificationConfiguration notification = mServiceConfiguration.getAccountNotificationConfiguration();

        if (!notification.isForegroundNotificationStatic()) {
            if (mAccounts.size() > 0) {
                PjSipAccount account = mAccounts.get(0);
                notification = notification.copy(account.getConfiguration().getUsername(), null);
            }
        }

        startForeground(NOTIFICATION_ACTIVE_REGISTRATION, buildNotification(notification));
    }

    private void doStopForeground() {
        stopForeground(true);
    }

    void emmitRegistrationChanged(PjSipAccount account, OnRegStateParam prm) {
        // Update foreground notification
        if (mConnectivityAvailable && mServiceConfiguration.getAccountNotificationConfiguration().isEnabled()) {
            doStartForeground();
        }

        getEmitter().fireRegistrationChangeEvent(account);
    }

    void emmitMessageReceived(PjSipAccount account, PjSipMessage message) {
        getEmitter().fireMessageReceivedEvent(message);
    }

    void emmitCallReceived(PjSipAccount account, PjSipCall call) {
        final int callId = call.getId();

        // Automatically decline incoming call when user uses GSM
        if (!mGSMIdle) {
            try {
                call.hangup(new CallOpParam(true));
            } catch (Exception e) {
                Log.w(TAG, "Failed to decline incoming call when user uses GSM", e);
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
                intent.putExtra("foreground", true);

                startActivity(intent);
            } catch (Exception e) {
                Log.w(TAG, "Failed to open application on received call", e);
            }
        }

        job(new Runnable() {
            @Override
            public void run() {
                // Brighten screen at least 10 seconds
                PowerManager.WakeLock wl = mPowerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE | PowerManager.FULL_WAKE_LOCK,
                    "incoming_call"
                );
                wl.acquire(10000);

                // Play ring sound to speaker or beep if there more than one call
                mCallsRingerLocks.add(callId);

                if (mCallsRingerLocks.size() == 1) {
                    startRinging(mCalls.size() > 1);
                }

                if (mCalls.size() == 0) {
                    mAudioManager.setSpeakerphoneOn(true);
                }
            }
        });

        // Show call notification
        if (mServiceConfiguration.getCallNotificationConfiguration().isEnabled()) {
            Notification notification = buildCallNotification(mServiceConfiguration.getCallNotificationConfiguration(), account, call);
            mNotificationManager.notify(NOTIFICATION_ACTIVE_CALL, notification);
        }

        // -----
        mCalls.add(call);
        mEmitter.fireCallReceivedEvent(call);
    }

    void emmitCallStateChanged(PjSipCall call, OnCallStateParam prm) {
        try {
            if (call.getInfo().getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                emmitCallTerminated(call, prm);
            } else {
                emmitCallChanged(call, prm);
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to handle call state event", e);
        }

        if (!mProximitySensorTracked) {
            startProximityTracking();
        }
    }

    void emmitCallChanged(PjSipCall call, OnCallStateParam prm) {
        try {
            final int callId = call.getId();
            final pjsip_inv_state callState = call.getInfo().getState();

            job(new Runnable() {
                @Override
                public void run() {
                    // Acquire wake lock
                    if (mIncallWakeLock == null) {
                        mIncallWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,	"incall");
                    }
                    if (!mIncallWakeLock.isHeld()) {
                        mIncallWakeLock.acquire();
                    }

                    // Ensure that ringing sound is stopped
                    if (callState != pjsip_inv_state.PJSIP_INV_STATE_INCOMING) {
                        if (mCallsRingerLocks.contains(callId)) {
                            mCallsRingerLocks.remove(callId);
                            if (mCallsRingerLocks.size() == 0) {
                                stopRinging();
                            } else {
                                startRinging(true);
                            }
                        }

                        if (!mUseSpeaker && mAudioManager.isSpeakerphoneOn()) {
                            mAudioManager.setSpeakerphoneOn(false);
                        }
                    }

                    // Acquire wifi lock
                    mWifiLock.acquire();

                    if (callState == pjsip_inv_state.PJSIP_INV_STATE_EARLY || callState == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                    }
                }
            });
        } catch (Exception e) {
            Log.w(TAG, "Failed to retrieve call state", e);
        }

        mEmitter.fireCallChanged(call);
    }

    void emmitCallTerminated(PjSipCall call, OnCallStateParam prm) {
        final int callId = call.getId();

        job(new Runnable() {
            @Override
            public void run() {
                // Stop play ring sound
                if (mCallsRingerLocks.contains(callId)) {
                    mCallsRingerLocks.remove(callId);
                    if (mCallsRingerLocks.size() == 0) {
                        stopRinging();
                    }
                }

                // Release wake lock
                if (mCalls.size() == 1) {
                    if (mIncallWakeLock != null && mIncallWakeLock.isHeld()) {
                        mIncallWakeLock.release();
                    }
                }

                // Release wifi lock
                if (mCalls.size() == 1) {
                    mWifiLock.release();
                }

                // Reset audio settings
                if (mCalls.size() == 1) {
                    mAudioManager.setSpeakerphoneOn(false);
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                }

                // Release proximity sensor
                if (mCalls.size() == 1 && mProximitySensorTracked) {
                    stopProximityTracking();
                }
            }
        });

        mEmitter.fireCallTerminated(call);
        evict(call);
    }

    void emmitCallUpdated(PjSipCall call) {
        mEmitter.fireCallChanged(call);
    }

    private Notification buildNotification(ServiceNotificationConfiguration config) {
        Intent foregroundIntent = buildForegroundIntent();
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, foregroundIntent, 0);
        NotificationCompat.Builder b = buildNotificationBuilder(config);

        if (config.getSmallIcon() == null || config.getSmallIcon().length() == 0) {
            Resources resources = getApplicationContext().getResources();

            int smallIconId = resources.getIdentifier("ic_launcher", "mipmap", getApplicationContext().getPackageName());
            if (smallIconId > 0) {
                b.setSmallIcon(smallIconId);
            }
        }

        return b.setContentIntent(pendIntent).setWhen(0).build();
    }

    private Notification buildCallNotification(ServiceNotificationConfiguration config, PjSipAccount account, PjSipCall call) {
        if (!config.isForegroundNotificationStatic()) {
            String title = "Call in Progress" + " - " + account.getConfiguration().getName();
            String text = null;
            try {
                text = call.getInfo().getRemoteUri();
            } catch (Exception e) {
                Log.w(TAG, "Failed to retrieve Remote URI of call", e);
            }

            config = config.copy(title, text);
        }

        return buildCallNotification(config, call);
    }

    private Notification buildCallNotification(ServiceNotificationConfiguration config, PjSipCall call) {
        Intent intent = buildForegroundIntent();
        intent.putExtra("call", call.getId());
        PendingIntent pendIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder b = buildNotificationBuilder(config);
        b.setOngoing(true);
        b.setWhen(System.currentTimeMillis());

        if (config.getSmallIcon() == null || config.getSmallIcon().length() == 0) {
            b.setSmallIcon(android.R.drawable.stat_sys_phone_call);
        }

        return b.setContentIntent(pendIntent).build();
    }

    private NotificationCompat.Builder buildNotificationBuilder(ServiceNotificationConfiguration config) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);

        if (config.getTitle() != null && config.getTitle().length() > 0) {
            b.setContentTitle(config.getTitle());
        }
        if (config.getText() != null && config.getText().length() > 0) {
            b.setContentText(config.getText());
        }
        if (config.getInfo() != null && config.getInfo().length() > 0) {
            b.setContentInfo(config.getInfo());
        }
        if (config.getTicker() != null && config.getTicker().length() > 0) {
            b.setTicker(config.getTicker());
        }

        Resources resources = getApplicationContext().getResources();

        if (config.getSmallIcon() != null && config.getSmallIcon().length() > 0) {
            int smallIconId = resources.getIdentifier(config.getSmallIcon(), "mipmap", getApplicationContext().getPackageName());
            if (smallIconId > 0) {
                b.setSmallIcon(smallIconId);
            }
        }

        if (config.getLargeIcon() != null && config.getLargeIcon().length() > 0) {
            int largeIconId = resources.getIdentifier(config.getLargeIcon(), "mipmap", getApplicationContext().getPackageName());

            if (largeIconId > 0) {
                Bitmap largeIconBitmap = BitmapFactory.decodeResource(resources, largeIconId);
                b.setLargeIcon(largeIconBitmap);
            }
        }

        return b;
    }

    private Intent buildForegroundIntent() {
        String foregroundIntentName = getApplicationContext().getPackageName() + ".MainActivity";
        Class<?> foregroundIntentCls = null;

        try {
            foregroundIntentCls = Class.forName(foregroundIntentName);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Could not found main activity class, please check whether \""+ foregroundIntentName +"\" available", e);
            throw new RuntimeException(e);
        }

        Intent foregroundIntent = new Intent(this, foregroundIntentCls);
        foregroundIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        return foregroundIntent;
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
                Log.w(TAG, "Failed to put call on hold", e);
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
                Log.w(TAG, "Failed to put call on hold", e);
            }
        }
    }

    /**
     *
     */
    private synchronized void startRinging(boolean ringBack)  {
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        try {
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE && mVibrator != null) {
                long[] pattern = {0,1000,1000};
                mVibrator.vibrate(pattern, 1);
            } else if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && mRingerPlayer == null) {
                // TODO: Add ability to set resource id from javascript
                // TODO: Add ability to play default sound
                // Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                // Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                // r.play();

                int soundId = ringBack ? R.raw.ringback : R.raw.ring;
                AssetFileDescriptor soundDesc = getResources().openRawResourceFd(soundId);

                mRingerPlayer = new MediaPlayer();
                mRingerPlayer.setDataSource(soundDesc.getFileDescriptor(), soundDesc.getStartOffset(), soundDesc.getLength());
                mRingerPlayer.prepare();
                mRingerPlayer.setVolume(1.0f, 1.0f);
                mRingerPlayer.setLooping(true);
                mRingerPlayer.start();
            } else {
                Log.w(TAG, "Failed to start ringing (already ringing)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start ringing", e);
        }
    }

    /**
     *
     */
    private synchronized void stopRinging() {
        if (mRingerPlayer != null) {
            mRingerPlayer.stop();
            mRingerPlayer.release();
            mRingerPlayer = null;
        }

        if (mVibrator != null) {
            mVibrator.cancel();
        }

        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    public synchronized void startProximityTracking() {
        if (mProximitySensor != null && !mProximitySensorTracked) {
            // Fall back to manual mode
            mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            mProximityFirstRun = true;
            mProximitySensorTracked = true;
        }
    }

    public synchronized void stopProximityTracking() {
        if (mProximitySensor != null && mProximitySensorTracked) {
            mProximitySensorTracked = false;
            mSensorManager.unregisterListener(this);
        }

        emmitCallScreenLock(false);
    }

    private void emmitCallScreenLock(boolean lock) {
        mEmitter.fireCallScreenLocked(lock);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mProximitySensorTracked && !mProximityFirstRun) {
            float distance = event.values[0];
            boolean active = (distance >= 0.0 && distance < PROXIMITY_THRESHOLD && distance < event.sensor.getMaximumRange());

            emmitCallScreenLock(active);
        }

        if (mProximityFirstRun) {
            mProximityFirstRun = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    protected class PhoneStateChangedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String extraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            if (TelephonyManager.EXTRA_STATE_RINGING.equals(extraState) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(extraState)) {
                Log.d(TAG, "GSM call received, pause all SIP calls and do not accept incoming SIP calls.");

                mGSMIdle = false;

                job(new Runnable() {
                    @Override
                    public void run() {
                        doPauseAllCalls();
                    }
                });

                stopRinging();
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(extraState)) {
                Log.d(TAG, "GSM call released, allow to accept incoming calls.");
                mGSMIdle = true;
            }
        }
    }
}
