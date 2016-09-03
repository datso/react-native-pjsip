'use strict';

import React, {
    DeviceEventEmitter,
    NativeModules,
    AppState,
} from 'react-native'
import {
    Record
} from 'immutable'

export default class Call extends Record({
    id: -1,
    callId: -1,
    accountId: -1,

    localContact: null,
    localUri: null,
    remoteContact: null,
    remoteUri: null,
    state: null,
    stateText: null,

    connectDuration: 0,
    totalDuration: 0,

    remoteOfferer: null,
    remoteAudioCount: 0,
    remoteVideoCount: 0,
    audioCount: 0,
    videoCount: 0
}) {

    constructor(data) {
        super(data);
        this._updateTime = Math.round(new Date().getTime() / 1000);
    }

    update(data) {
        this._updateTime = Math.round(new Date().getTime() / 1000);
        super.update(data);
    }

    getId() {
        return this.get('id');
    }

    getCallId() {
        return this.get('callId');
    }

    getAccountId() {
        return this.get('accountId');
    }

    isHeld() {
        // TODO
    }

    /**
     * Returns a duration of call in seconds.
     *
     * @public
     * @returns {int}
     */
    getDuration() {
        let time = Math.round(new Date().getTime() / 1000);
        let offset = time - this._updateTime;

        return this.get('totalDuration') + offset;
    };

    /**
     * Returns a duration in "MM:SS" format.
     *
     * @public
     * @returns {string}
     */
    getFormattedDuration() {
        var seconds = this.getDuration();
        if (isNaN(seconds)) {
            return "00:00";
        }
        var hours = parseInt( seconds / 3600 ) % 24;
        var minutes = parseInt( seconds / 60 ) % 60;
        var result = "";
        seconds = seconds % 60;

        if (hours > 0) {
            result += (hours < 10 ? "0" + hours : hours) + ":";
        }

        result += (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds  < 10 ? "0" + seconds : seconds);
        return result;
    };


    answer() {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.answerCall(this.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    hangup() {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.hangupCall(this.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    hold() {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.holdCall(this.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    unhold() {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.unholdCall(this.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    xfer(destination) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.xferCall(this.getId(), destination, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    dtmf(digits) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.dtmfCall(this.getId(), digits, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    // getInfo
    // ... getId
    // ... getAccId
    // ... getLocalUri
    // ... getLocalContact
    // ... getRemoteUri
    // ... getRemoteContact
    // ... getCallIdString
    // ... getSetting
    // ... ... getAudioCount
    // ... ... getVideoCount
    // ... getState
    // ... getStateText
    // ... getLastStatusCode
    // ... getLastReason
    // ... getMedia
    // ... ... getIndex
    // ... ... getType
    // ... ... getDir
    // ... ... getStatus
    // ... ... getAudioConfSlot
    // ... ... getVideoIncomingWindowId
    // ... ... getVideoCapDev
    // ... getProvMedia
    // ... ... getIndex
    // ... ... getType
    // ... ... getDir
    // ... ... getStatus
    // ... ... getAudioConfSlot
    // ... ... getVideoIncomingWindowId
    // ... ... getVideoCapDev
    // ... getConnectDuration
    // ... getTotalDuration
    // ... getRemOfferer
    // ... getRemAudioCount
    // ... getRemVideoCount

    // getMedia

    // getMedTransportInfo


    // --------------------
    // Could be set to: retrieveStreamsStats()
    // --------------------

    // getStreamInfo (med_idx)
    // ... getType
    // ... getProto
    // ... getDir
    // ... getRemoteRtpAddress
    // ... getRemoteRtcpAddress
    // ... getTxPt
    // ... getRxPt
    // ... getCodecName
    // ... getCodecClockRate
    // getStreamStat (med_idx)
    // ... getRtcp
    // ... getJbuf


    // public void answer(CallOpParam prm) throws java.lang.Exception {
    // public void hangup(CallOpParam prm) throws java.lang.Exception {
    // public void hold(CallOpParam prm) throws java.lang.Exception {
    // public void unhold(CallOpParam prm) throws java.lang.Exception {

    // public void mute(CallOpParam prm) throws java.lang.Exception {
    // public void unmute(CallOpParam prm) throws java.lang.Exception {

    // public void reinvite(CallOpParam prm) throws java.lang.Exception {
    // public void update(CallOpParam prm) throws java.lang.Exception {

    // public void xfer(String dest, CallOpParam prm) throws java.lang.Exception {
    // public void dtmf(String digits) throws java.lang.Exception {

    // public void dump(String digits) throws java.lang.Exception {


}