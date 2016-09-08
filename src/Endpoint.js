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
     * Add a new account. If registration is configured for this account, this function would also start the
     * SIP registration session with the SIP registrar server. This SIP registration session will be maintained
     * internally by the library, and application doesn't need to do anything to maintain the registration session.
     *
     * An example configuration:
     * {
     *   name: "John Doe",
     *   username: "100",
     *   domain: "pbx.com",
     *   password: "XXXXXX",
     *
     *   proxy: "192.168.100.1:5060", // default disabled.
     *   transport: "TCP", // default TCP
     *   regServer: "pbx.com", // default taken from domain
     *   regTimeout: 300, // default 300
     * }
     *
     * @param {Object} configuration
     * @returns {Promise}
     */
    createAccount(configuration) {
        return new Promise(function(resolve, reject) {
            NativeModules.PjSipModule.createAccount(configuration, (successful, data) => {

                console.log("createAccount response", successful, data);

                if (successful) {
                    resolve(new Account(data));
                } else {
                    reject(data);
                }
            });
        });
    }

    replaceAccount(account, configuration) {
        throw new Error("Not implemented");
    }

    /**
     * Delete an account. This will unregister the account from the SIP server, if necessary, and terminate server side presence subscriptions associated with this account.
     *
     * @param {Account} account
     * @returns {Promise}
     */
    deleteAccount(account) {
        return new Promise(function(resolve, reject) {
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
     * Make outgoing call to the specified URI.
     *
     * @param account {Account}
     * @param destination {String} Destination SIP URI.
     */
    makeCall(account, destination) {
        if (!destination.startsWith("sip:")) {
            let realm = account.getRegServer();

            if (!realm) {
                realm = account.getDomain();
                let s = realm.indexOf(":");

                if (s > 0) {
                    realm = realm.substr(0, s + 1);
                }
            }

            destination = "sip:" + destination + "@" + realm;
        }

        return new Promise(function(resolve, reject) {
            NativeModules.PjSipModule.makeCall(account.getId(), destination, (successful, data) => {
                if (successful) {
                    resolve(new Call(data));
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Send response to incoming INVITE request.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    answerCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.answerCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Hangup call by using method that is appropriate according to the call state.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    hangupCall(call) {
        // TODO: Add possibility to pass code and reason for hangup.
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.hangupCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Put the specified call on hold. This will send re-INVITE with the appropriate SDP to inform remote that the call is being put on hold.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    holdCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.holdCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Release the specified call from hold. This will send re-INVITE with the appropriate SDP to inform remote that the call is resumed.
     *
     * @param call {Call} Call instance
     * @returns {Promise}
     */
    unholdCall(call) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.unholdCall(call.getId(), (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Initiate call transfer to the specified address.
     * This function will send REFER request to instruct remote call party to initiate a new INVITE session to the specified destination/target.
     *
     * @param call {Call} The call to be transferred.
     * @param destination URI of new target to be contacted. The URI may be in name address or addr-spec format.
     * @returns {Promise}
     */
    xferCall(call, destination) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.xferCall(call.getId(), destination, (successful, data) => {
                if (successful) {
                    resolve(data);
                } else {
                    reject(data);
                }
            });
        });
    }

    /**
     * Send DTMF digits to remote using RFC 2833 payload formats.
     *
     * @param call {Call} Call instance
     * @param digits {String} DTMF string digits to be sent as described on RFC 2833 section 3.10.
     * @returns {Promise}
     */
    dtmfCall(call, digits) {
        return new Promise((resolve, reject) => {
            NativeModules.PjSipModule.dtmfCall(call.getId(), digits, (successful, data) => {
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
        this.emit("call_changed", new Call(data));
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
        this.emit("call_terminated", new Call(data));
    }

    // setUaConfig(UaConfig value)
    // setMaxCalls
    // setUserAgent
    // setNatTypeInSdp

    // setLogConfig(LogConfig value)
    // setLevel
}
