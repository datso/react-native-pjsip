import AccountRegistration from './AccountRegistration'

/**
 * This describes account configuration and registration status
 */
export default class Account {

    constructor(data) {
        this._data = data;
        this._registration = new AccountRegistration(data['registration']);
    }

    /**
     * The account ID.
     * @returns {int}
     */
    getId() {
        return this._data.id;
    }

    /**
     * This is the URL to be put in the request URI for the registration, and will look something like "sip:serviceprovider".
     * @returns {String}
     */
    getURI() {
        return this._data.uri;
    }

    /**
     * Full name specified in Endpoint.createAccount().
     * @returns {String}
     */
    getName() {
        return this._data.name;
    }

    /**
     * Username specified in Endpoint.createAccount().
     * @returns {String}
     */
    getUsername() {
        return this._data.username;
    }

    /**
     * Domain specified in Endpoint.createAccount().
     * @returns {int|null}
     */
    getDomain() {
        return this._data.domain;
    }

    /**
     * Password specified in Endpoint.createAccount().
     * @returns {String}
     */
    getPassword() {
        return this._data.password;
    }

    /**
     * Proxy specified in Endpoint.createAccount().
     * @returns {String}
     */
    getProxy() {
        return this._data.proxy;
    }

    /**
     * Transport specified in Endpoint.createAccount().
     * @returns {String}
     */
    getTransport() {
        return this._data.transport;
    }

    /**
     * Additional parameters that will be appended in the Contact header
     * for this account.
     * @returns {String}
     */
    getContactParams() {
        return this._data.contactParams;
    }

    /**
     * Additional URI parameters that will be appended in the Contact URI
     * for this account.
     * @returns {String}
     */
    getContactUriParams() {
        return this._data.contactUriParams;
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {String}
     */
    getRegServer() {
        return this._data.regServer || "";
    }

    /**
     * Port specified in Endpoint.createAccount().
     * @returns {String}
     */
    getRegTimeout() {
        return this._data.regTimeout;
    }

    /**
     * @returns {String}
     */
    getRegContactParams() {
        return this._data.regContactParams;
    }

    /**
     * @returns {Object}
     */
    getRegHeaders() {
        return this._data.regHeaders;
    }

    /**
     * Account registration status.
     * @returns {AccountRegistration}
     */
    getRegistration() {
        return this._registration;
    }
}