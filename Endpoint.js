'use strict';

import React, {
    DeviceEventEmitter,
    NativeModules,
} from 'react-native';
import {EventEmitter} from 'events'

import Call from './Call'
import Account from './Account'

export default class Endpoint extends EventEmitter {

    constructor() {
        super();

        // Subscribe to Accounts events
        DeviceEventEmitter.addListener('pjSipRegistrationChanged', this._onRegistrationChanged.bind(this));

        // Subscribe to Calls events
        DeviceEventEmitter.addListener('pjSipCallReceived', this._onCallReceived.bind(this));
        DeviceEventEmitter.addListener('pjSipCallChanged', this._onCallChanged.bind(this));
        DeviceEventEmitter.addListener('pjSipCallTerminated', this._onCallTerminated.bind(this));
    }

    /**
     * Returns a Promise that will be resolved once PjSip module is initialized.
     * Do not call any function while library is not initialized.
     *
     * @returns {Promise}
     */
    start() {
        return new Promise(function(resolve, reject) {
            NativeModules.PjSipModule.start((successful, data) => {
                if (successful) {
                    let accounts = [];
                    let calls = [];

                    if (data.hasOwnProperty('accounts')) {
                        for (let d of data['accounts']) {
                            accounts.push(new Account(d));
                        }
                    }

                    if (data.hasOwnProperty('calls')) {
                        for (let d of data['calls']) {
                            calls.push(new Call(d));
                        }
                    }

                    resolve({
                        accounts,
                        calls
                    });
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param {AccountConfig|Object} configuration
     * @returns {Promise}
     */
    createAccount(configuration) {
        return new Promise(function(resolve, reject) {
            NativeModules.PjSipModule.createAccount(configuration, (successful, data) => {
                if (successful) {
                    resolve(new Account(data));
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @param {Account} account
     * @returns {Promise}
     */
    deleteAccount(account) {
        return new Promise(function(resolve, reject) {


            console.log("NativeModules.PjSipModule.deleteAccount", account.getId());

            NativeModules.PjSipModule.deleteAccount(account.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * @fires Endpoint#registration_changed
     * @private
     * @param data {Object}
     */
    _onRegistrationChanged(data) {
        /**
         * Fires when registration status has changed.
         *
         * @event Endpoint#registration_changed
         * @property {Account} account
         */
        this.emit("registration_changed", new Account(data));
    }

    /**
     * @fires Endpoint#call_received
     * @private
     * @param data {Object}
     */
    _onCallReceived(data) {


        /**
         * TODO
         *
         * @event Endpoint#call_received
         * @property {Call} call
         */
        this.emit("call_received", new Call(data));
    }

    /**
     * @fires Endpoint#call_changed
     * @private
     * @param data {Object}
     */
    _onCallChanged(data) {
        /**
         * TODO
         *
         * @event Endpoint#call_changed
         * @property {Call} call
         */
        this.emit("call_received", new Call(data));
    }

    /**
     * @fires Endpoint#call_terminated
     * @private
     * @param data {Object}
     */
    _onCallTerminated(data) {
        /**
         * TODO
         *
         * @event Endpoint#call_terminated
         * @property {Call} call
         */
        this.emit("call_received", new Call(data));
    }

    // setUaConfig(UaConfig value)
    // setMaxCalls
    // setUserAgent
    // setNatTypeInSdp

    // setLogConfig(LogConfig value)
    // setLevel
}