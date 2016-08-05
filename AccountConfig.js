export const TRANSPORT_UDP = "UDP";
export const TRANSPORT_TCP = "TCP";
export const TRANSPORT_TLS = "TLS";

/**
 *
 */
export class AccountConfig {
    username;
    password;
    host;
    realm;
    port;
    transport;

    // TODO: Proxy configuration

    constructor({username, password, host, realm = null, port = 5060, transport = TRANSPORT_TCP}) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.realm = realm;
        this.port = port;
        this.transport = transport;
    }

    setUsername(username) {
        this.username = username;
        return this;
    }

    setPassword(password) {
        this.password = password;
        return this;
    }

    setHost(host) {
        this.host = host;
        return this;
    }

    setRealm(realm) {
        this.realm = realm;
        return this;
    }

    setPort(port) {
        this.port = port;
        return this;
    }

    setTransport(transport) {
        this.transport = transport;
        return this;
    }

    setRegConfig() {
        throw new Error("Not implemented");
    }

    setSipConfig() {
        throw new Error("Not implemented");
    }

    setPresConfig() {
        throw new Error("Not implemented");
    }

    setNatConfig() {
        throw new Error("Not implemented");
    }

    setMediaConfig() {
        throw new Error("Not implemented");
    }

    setVideoConfig() {
        throw new Error("Not implemented");
    }

    build() {
        return {

        }
    }

}