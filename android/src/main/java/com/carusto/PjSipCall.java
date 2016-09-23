package com.carusto;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import org.json.JSONObject;
import org.pjsip.pjsua2.*;

public class PjSipCall extends Call {

    private static String TAG = "PjSipCall";

    private PjSipAccount account;

    private boolean isHeld = false;

    private boolean isMuted = false;

    public PjSipCall(PjSipAccount acc, int call_id) {
        super(acc, call_id);
        this.account = acc;
    }

    public PjSipCall(PjSipAccount acc) {
        super(acc);
        this.account = acc;
    }

    public PjSipService getService() {
        return account.getService();
    }

    public void hold() throws Exception {
        if (isHeld) {
            return;
        }

        isHeld = true;

        // Emmit changes
        getService().emmitCallUpdated(this);

        // Send reinvite to server for hold
        setHold(new CallOpParam(true));
    }

    public void unhold() throws Exception {
        if (!isHeld) {
            return;
        }

        isHeld = false;

        // Emmit changes
        getService().emmitCallUpdated(this);

        // Send reinvite to server for release from hold
        CallOpParam prm = new CallOpParam(true);
        prm.getOpt().setFlag(1);

        reinvite(prm);
    }

    public void mute() throws Exception {
        if (isMuted) {
            return;
        }

        isMuted = true;
        doMute(true);

        // Emmit changes
        getService().emmitCallUpdated(this);
    }

    public void unmute() throws Exception {
        if (!isMuted) {
            return;
        }

        isMuted = false;
        doMute(false);

        // Emmit changes
        getService().emmitCallUpdated(this);
    }

    private void doMute(boolean mute) throws Exception {
        CallInfo info = getInfo();

        for (int i = 0; i < info.getMedia().size(); i++) {
            Media media = getMedia(i);
            CallMediaInfo mediaInfo = info.getMedia().get(i);

            if (media != null &&
                    mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO  &&
                    mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                try {
                    audioMedia.adjustRxLevel((float) (mute ? 0 : 1));
                } catch (Exception exc) {
                    Log.e(TAG, "Error while adjusting levels", exc);
                }
            }
        }
    }

    public void redirect(String destination) throws Exception {
        SipHeader contactHeader = new SipHeader();
        contactHeader.setHName("Contact");
        contactHeader.setHValue(destination);

        SipHeaderVector contactHeaders = new SipHeaderVector();
        contactHeaders.add(contactHeader);

        SipTxOption tx = new SipTxOption();
        tx.setHeaders(contactHeaders);

        CallOpParam prm = new CallOpParam();
        prm.setStatusCode(pjsip_status_code.PJSIP_SC_MOVED_TEMPORARILY);
        prm.setTxOption(tx);

        answer(prm);
    }

    @Override
    public void onCallState(OnCallStateParam prm) {
        try {
            getService().emmitCallStateChanged(this, prm);
        } catch (Exception e) {
            Log.e(TAG, "Exception for onCallState callback", e);
        }
    }

    @Override
    public void onCallMediaState(OnCallMediaStateParam prm) {
        try {
            CallInfo info = getInfo();

            for (int i = 0; i < info.getMedia().size(); i++) {
                Media media = getMedia(i);
                CallMediaInfo mediaInfo = info.getMedia().get(i);

                if (mediaInfo.getType() == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                        media != null &&
                        mediaInfo.getStatus() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
                    AudioMedia audioMedia = AudioMedia.typecastFromMedia(media);

                    // connect the call audio media to sound device
                    AudDevManager mgr = account.getService().getAudDevManager();
                    audioMedia.adjustRxLevel((float) 1.0);
                    audioMedia.adjustTxLevel((float) 1.0);
                    audioMedia.startTransmit(mgr.getPlaybackDevMedia());
                    mgr.getCaptureDevMedia().startTransmit(audioMedia);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to start transmit to playback device", e);
        }

        // Emmit changes
        getService().emmitCallUpdated(this);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            CallInfo info = getInfo();

            // -----
            AudioManager audioManager = (AudioManager) getService().getBaseContext().getSystemService(Context.AUDIO_SERVICE);
            boolean speaker = audioManager.isSpeakerphoneOn();

            // -----
            int connectDuration = -1;

            if (info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED ||
                info.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                connectDuration = info.getConnectDuration().getSec();
            }

            // -----
            json.put("id", getId());
            json.put("callId", info.getCallIdString());
            json.put("accountId", account.getId());

            // -----
            json.put("localContact", info.getLocalContact());
            json.put("localUri", info.getLocalUri());
            json.put("remoteContact", info.getRemoteContact());
            json.put("remoteUri", info.getRemoteUri());

            // -----
            json.put("state", info.getState());
            json.put("stateText", info.getStateText());
            json.put("connectDuration", connectDuration);
            json.put("totalDuration", info.getTotalDuration().getSec());
            json.put("held", isHeld);
            json.put("muted", isMuted);
            json.put("speaker", speaker);

            try {
                json.put("lastStatusCode", info.getLastStatusCode());
            } catch (Exception e) {
                json.put("lastStatusCode", null);
            }

            json.put("lastReason", info.getLastReason());

            // -----
            json.put("remoteOfferer", info.getRemOfferer());
            json.put("remoteAudioCount", info.getRemAudioCount());
            json.put("remoteVideoCount", info.getRemVideoCount());

            // -----
            json.put("audioCount", info.getSetting().getAudioCount());
            json.put("videoCount", info.getSetting().getVideoCount());

            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String toJsonString() {
        return toJson().toString();
    }
}
