'use strict';

import React, {
    DeviceEventEmitter,
    NativeModules,
    AppState,
} from 'react-native'

import {
    Record
} from 'immutable'

import AccountRegistration from './AccountRegistration'
import Call from './Call'

/**
 * Class that represent pj_sip_account.
 *
 * Constructor data:
 *
 * Fields:
 * - getId
 * - getInfo
 * - ... public String getUri()
 * - ... public boolean getRegIsConfigured()
 * - ... public boolean getRegIsActive()
 * - ... public int getRegExpiresSec()
 * - ... public pjsip_status_code getRegStatus()
 * - ... public String getRegStatusText()
 * - ... public int getRegLastErr()
 * - ... public boolean getOnlineStatus()
 * - ... public String getOnlineStatusText()
 *
 * - getCalls
 *
 * makeCall
 *
 * modify
 * setRegistration
 * setOnlineStatus
 *
 * public void onIncomingCall(OnIncomingCallParam prm) {
 * public void onRegStarted(OnRegStartedParam prm) {
 * public void onRegState(OnRegStateParam prm) {
 * public void onIncomingSubscribe(OnIncomingSubscribeParam prm) {
 * public void onInstantMessage(OnInstantMessageParam prm) {
 * public void onInstantMessageStatus(OnInstantMessageStatusParam prm) {
 * public void onTypingIndication(OnTypingIndicationParam prm) {
 * public void onMwiInfo(OnMwiInfoParam prm) {
 *
 */
export default class Account extends Record({
    id: -1,
    uri: null,
    registration: null
}) {

    // this._registration = new AccountRegistration(registration);

    getId() {
        return this.get('id');
    }

    getURI() {
        return this.get('uri');
    }

    /**
     * @returns {AccountRegistration}
     */
    getRegistration() {
        return this.get('registration');
    }

    /**
     * Make outgoing call to the specified URI.
     *
     * @param destination {String} Destination SIP URI.
     * @param headers {Object[]} Optional list of headers to be sent with outgoing INVITE.
     */
    makeCall(destination, headers = []) {
        let id = this.getId();
        let self = this;

        return new Promise(function(resolve, reject) {
            NativeModules.PjSipModule.makeCall(id, destination, (successful, data) => {
                if (successful) {
                    resolve(new Call(data));
                } else {
                    reject(data);
                }
            });
        });
    }
}