package com.carusto.configuration;

import android.content.Intent;
import android.support.annotation.Nullable;

public class AccountConfiguration {

    public String name;

    public String username;

    public String domain;

    public String password;

    public String proxy;

    public String transport;

    public String regServer;

    @Nullable
    public Integer regTimeout;

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getDomain() {
        return domain;
    }

    public String getPassword() {
        return password;
    }

    public String getProxy() {
        return proxy;
    }

    public String getTransport() {
        return transport;
    }

    public String getRegServer() {
        return regServer;
    }

    public String getNomalizedRegServer() {
        return regServer != null && regServer.length() > 0 ? regServer : "*";
    }

    @Nullable
    public Integer getRegTimeout() {
        return regTimeout;
    }

    public String getRegUri() {
        return "sip:"+ domain;
    }

    public String getIdUri() {
        return name + " <sip:"+ username +"@"+ domain +">";
    }

    public boolean isTransportNotEmpty() {
        return transport != null && !transport.isEmpty() && !transport.equals("TCP");
    }

    public boolean isRegTimeoutNotEmpty() {
        return this.regTimeout != null && this.regTimeout != 0;
    }

    public boolean isProxyNotEmpty() {
        return proxy != null && proxy.length() > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountConfiguration that = (AccountConfiguration) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;
        if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) return false;
        if (transport != null ? !transport.equals(that.transport) : that.transport != null) return false;
        if (regServer != null ? !regServer.equals(that.regServer) : that.regServer != null) return false;
        return regTimeout != null ? regTimeout.equals(that.regTimeout) : that.regTimeout == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (domain != null ? domain.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + (transport != null ? transport.hashCode() : 0);
        result = 31 * result + (regServer != null ? regServer.hashCode() : 0);
        result = 31 * result + (regTimeout != null ? regTimeout.hashCode() : 0);
        return result;
    }

    public static AccountConfiguration fromIntent(Intent intent) {
        AccountConfiguration c = new AccountConfiguration();
        c.name = intent.getStringExtra("name");
        c.username = intent.getStringExtra("username");
        c.domain = intent.getStringExtra("domain");
        c.password = intent.getStringExtra("password");
        c.proxy = intent.getStringExtra("proxy");
        c.transport = intent.getStringExtra("transport");
        c.regServer = intent.getStringExtra("regServer");
        c.regTimeout = null;

        if (intent.hasExtra("regTimeout")) {
            String regTimeout = intent.getStringExtra("regTimeout");

            if (regTimeout != null && !regTimeout.isEmpty()) {
                int timeout = Integer.parseInt(regTimeout);
                if (timeout > 0) {
                    c.regTimeout = timeout;
                }
            }
        }

        return c;
    }
}
