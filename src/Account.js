'use strict';

import AccountRegistration from './AccountRegistration'

/**
 * This describes account configuration and registration status
 */
export default class Account {
    constructor({id, uri, username, password, host, port, realm, registration}) {
        this._id = id;
        this._uri = uri;
        this._username = username;
        this._password = password;
        this._host = host;
        this._port = port;
        this._realm = realm;
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
     * Username specified in Endpoint.createAccount().
     * @returns {String}
     */
    getUsername() {
        return this._username;
    }

    /**
     * Password specified in Endpoint.createAccount().
     * @returns {String}
     */
    getPassword() {
        return this._password;
    }

    /**
     * Host specified in Endpoint.createAccount().
     * @returns {int|null}
     */
    getHost() {
        return this._host;
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {int|null}
     */
    getPort() {
        return this._port;
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {String}
     */
    getRealm() {
        return this._realm;
    }

    /**
     * Account registration status.
     * @returns {AccountRegistration}
     */
    getRegistration() {
        return this._registration;
    }
}