package com.carusto.ReactNativePjSip.dto;

import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.Map;

public class AccountConfigurationDTO {

    public String name;

    public String username;

    public String domain;

    public String password;

    public String proxy;

    public String transport;

    private String contactParams;

    private String contactUriParams;

    public String regServer;

    @Nullable
    public Integer regTimeout;

    public Map<String, String> regHeaders;

    public String regContactParams;

    public boolean regOnAdd;

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

    public String getContactParams() {
        return contactParams;
    }

    public String getContactUriParams() {
        return contactUriParams;
    }

    public String getRegServer() {
        return regServer;
    }

    public Map<String, String> getRegHeaders() {
        return regHeaders;
    }

    public String getRegContactParams() {
        return regContactParams;
    }

    public boolean isRegOnAdd() {
        return regOnAdd;
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
        if (name != null) {
            return name + " <sip:"+ username +"@"+ domain +">";
        }

        return "<sip:"+ username +"@"+ domain +">";
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

    public static AccountConfigurationDTO fromIntent(Intent intent) {
        AccountConfigurationDTO c = new AccountConfigurationDTO();
        c.name = intent.getStringExtra("name");
        c.username = intent.getStringExtra("username");
        c.domain = intent.getStringExtra("domain");
        c.password = intent.getStringExtra("password");
        c.proxy = intent.getStringExtra("proxy");
        c.transport = intent.getStringExtra("transport");
        c.contactParams = intent.getStringExtra("contactParams");
        c.contactUriParams = intent.getStringExtra("contactUriParams");

        c.regServer = intent.getStringExtra("regServer");
        c.regTimeout = 600;
        c.regOnAdd = intent.getBooleanExtra("regOnAdd", true);

        if (intent.hasExtra("regTimeout")) {
            String regTimeout = intent.getStringExtra("regTimeout");

            if (regTimeout != null && !regTimeout.isEmpty()) {
                int timeout = Integer.parseInt(regTimeout);
                if (timeout > 0) {
                    c.regTimeout = timeout;
                }
            }
        }

        c.regContactParams = intent.getStringExtra("regContactParams");

        if (intent.hasExtra("regHeaders")) {
            c.regHeaders = (Map<String, String>) intent.getSerializableExtra("regHeaders");
        }

        return c;
    }
}
