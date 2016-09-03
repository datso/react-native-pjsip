'use strict';

/**
 * This class describes the information and current status of a call.
 */
export default class Call {

    constructor({
            id, callId, accountId,
            localContact, localUri, remoteContact, remoteUri,
            state, stateText,
            connectDuration, totalDuration,
            remoteOfferer, remoteAudioCount, remoteVideoCount, audioCount, videoCount
        }) {

        this._id = id;
        this._callId = callId;
        this._accountId = accountId;
        this._localContact = localContact;
        this._localUri = localUri;
        this._remoteContact = remoteContact;
        this._remoteUri = remoteUri;
        this._state = state;
        this._stateText = stateText;
        this._connectDuration = connectDuration;
        this._totalDuration = totalDuration;
        this._remoteOfferer = remoteOfferer;
        this._remoteAudioCount = remoteAudioCount;
        this._remoteVideoCount = remoteVideoCount;
        this._audioCount = audioCount;
        this._videoCount = videoCount;

        this._constructionTime = Math.round(new Date().getTime() / 1000);
    }

    /**
     * Call identification.
     * @returns {int}
     */
    getId() {
        return this._id;
    }

    /**
     * The account ID where this call belongs.
     * @returns {int}
     */
    getAccountId() {
        return this._accountId;
    }

    /**
     * Dialog Call-ID string.
     *
     * @returns {String}
     */
    getCallId() {
        return this._callId;
    }


    /**
     * Up-to-date call duration in seconds.
     * Use local time to calculate actual call duration.
     *
     * @public
     * @returns {int}
     */
    getDuration() {
        let time = Math.round(new Date().getTime() / 1000);
        let offset = time - this._constructionTime;

        return this._totalDuration + offset;
    };

    /**
     * Up-to-date call connected duration (zero when call is not established)
     *
     * @returns {int}
     */
    getConnectDuration() {
        // TODO: Verify whether function correctly works
        return this._connectDuration;
    }

    /**
     * Call duration in "MM:SS" format.
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

    /**
     * Local Contact.
     * TODO: Provide example
     * @returns {String}
     */
    getLocalContact() {
        return this._localContact;
    }

    /**
     * Local URI.
     * TODO: Provide example
     * @returns {String}
     */
    getLocalUri() {
        return this._localUri;
    }

    /**
     * Remote contact.
     * TODO: Provide example
     * @returns {String}
     */
    getRemoteContact() {
        return this._remoteContact;
    }

    /**
     * Remote URI.
     * TODO: Provide example
     * @returns {String}
     */
    getRemoteUri() {
        return this._remoteUri;
    }

    /**
     * Invite session state.
     *
     * PJSIP_INV_STATE_NULL           Before INVITE is sent or received
     * PJSIP_INV_STATE_CALLING        After INVITE is sent
     * PJSIP_INV_STATE_INCOMING       After INVITE is received.
     * PJSIP_INV_STATE_EARLY          After response with To tag.
     * PJSIP_INV_STATE_CONNECTING     After 2xx is sent/received.
     * PJSIP_INV_STATE_CONFIRMED      After ACK is sent/received.
     * PJSIP_INV_STATE_DISCONNECTED   Session is terminated.
     *
     * @returns {String}
     */
    getState() {
        return this._state;
    }

    /**
     * Text describing the state.
     *
     * @returns {String}
     */
    getStateText() {
        return this._stateText;
    }

    isHeld() {
        // TODO
    }

    isTerminated() {
        // TODO
    }

    /**
     * Flag if remote was SDP offerer
     * @returns {boolean}
     */
    getRemoteOfferer() {
        // TODO Verify whether boolean value
        return this._remoteOfferer;
    }

    /**
     * Number of audio streams offered by remote.
     * @returns {int}
     */
    getRemoteAudioCount() {
        return this._remoteAudioCount;
    }

    /**
     * Number of video streams offered by remote.
     * @returns {int}
     */
    getRemoteVideoCount() {
        return this._remoteVideoCount;
    }

    /**
     * Number of simultaneous active audio streams for this call. If zero - audio is disabled in this call.
     * @returns {int}
     */
    getAudioCount() {
        return this._audioCount;
    }

    /**
     * Number of simultaneous active video streams for this call. If zero - video is disabled in this call.
     * @returns {*}
     */
    getVideoCount() {
        return this._videoCount;
    }

}