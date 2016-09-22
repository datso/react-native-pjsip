'use strict';

import AccountRegistration from './AccountRegistration'

/**
 * This describes account configuration and registration status
 */
export default class Account {
    constructor({id, uri, name, username, domain, password, proxy, transport, regServer, regTimeout, registration}) {
        this._id = id;
        this._uri = uri;
        this._name = name;
        this._username = username;
        this._domain = domain;
        this._password = password;
        this._proxy = proxy;
        this._transport = transport;
        this._regServer = regServer;
        this._regTimeout = regTimeout;
        this._registration = new AccountRegistration(registration);
    }

    /**
     * The account ID.
     * @returns {int}
     */
    getId() {
        return this._id;
    }

    /**
     * This is the URL to be put in the request URI for the registration, and will look something like "sip:serviceprovider".
     * @returns {String}
     */
    getURI() {
        return this._uri;
    }

    /**
     * Full name specified in Endpoint.createAccount().
     * @returns {String}
     */
    getName() {
        return this._name;
    }

    /**
     * Username specified in Endpoint.createAccount().
     * @returns {String}
     */
    getUsername() {
        return this._username;
    }

    /**
     * Domain specified in Endpoint.createAccount().
     * @returns {int|null}
     */
    getDomain() {
        return this._domain;
    }

    /**
     * Password specified in Endpoint.createAccount().
     * @returns {String}
     */
    getPassword() {
        return this._password;
    }

    /**
     * Proxy specified in Endpoint.createAccount().
     * @returns {String}
     */
    getProxy() {
        return this._proxy || "";
    }

    /**
     * Transport specified in Endpoint.createAccount().
     * @returns {String}
     */
    getTransport() {
        return this._transport || "";
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {String}
     */
    getRegServer() {
        return this._regServer || "";
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {String}
     */
    getRegTimeout() {
        return this._regTimeout;
    }

    /**
     * Account registration status.
     * @returns {AccountRegistration}
     */
    getRegistration() {
        return this._registration;
    }
}