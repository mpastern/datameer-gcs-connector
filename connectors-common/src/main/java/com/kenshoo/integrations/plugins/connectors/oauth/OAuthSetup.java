//
//   Copyright (c) 2014 Kenshoo.com
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//


package com.kenshoo.integrations.plugins.connectors.oauth;

import datameer.dap.sdk.common.GenericConfiguration;
import datameer.dap.sdk.property.CallbackRequest;
import datameer.dap.sdk.util.DatameerServer;
import datameer.dap.sdk.util.DateTimeUtil;
import datameer.dap.sdk.util.StringUtil;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class OAuthSetup
{
    public static final String PROPERTY_KEY_ACCESS_TOKEN = "authentication.oauth.accesstoken";
    public static final String PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME =
            "authentication.oauth.accesstoken.creation-time";
    public static final String PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER =
            "authentication.oauth.accesstoken.expires-after";

    private final String oauthServiceName;
    private final TokenRefresh tokenRefresh;
    private String scope;
    private OAuthClientData oAuthClientData;
    private long defaultTokenExpiration = TimeUnit.HOURS.toMillis(2L);

    public OAuthSetup(String oauthServiceName, TokenRefresh tokenRefresh) {
        this.oauthServiceName = oauthServiceName;
        this.tokenRefresh = tokenRefresh;
    }

    public String getOAuthServiceName() {
        return oauthServiceName;
    }

    public boolean isTokenRefreshSupported() {
        return tokenRefresh == TokenRefresh.SUPPORTED;
    }

    public long getDefaultTokenExpiration() {
        return defaultTokenExpiration;
    }

    public void setDefaultTokenExpiration(long tokenExpiration)
    {
        defaultTokenExpiration = tokenExpiration;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setOAuthClientData(OAuthClientData clientData) {
        oAuthClientData = clientData;
    }

    public String getClientId() {
        return oAuthClientData.getClientId();
    }

    public String getClientSecret() {
        return oAuthClientData.getSecret();
    }

    public String getRedirectUrl() {
        return oAuthClientData.getRedirectUri();
    }

    public String getAccessToken(GenericConfiguration configuration) {
        return configuration.getStringProperty(PROPERTY_KEY_ACCESS_TOKEN, null);
    }

    public Long getAccessTokenCreationTime(GenericConfiguration configuration) {
        return configuration.getLongProperty(PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME, null);
    }

    public long getTokenExpiresAfter(GenericConfiguration configuration) {
        return configuration.getLongProperty(PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER,
                Long.valueOf(getDefaultTokenExpiration())).longValue();
    }

    public long getTokenExpirationDate(GenericConfiguration configuration) {
        Long accessTokenCreationTime = getAccessTokenCreationTime(configuration);
        if (accessTokenCreationTime == null) {
            return 0L;
        }
        long expiresAfter = getTokenExpiresAfter(configuration);
        if (expiresAfter <= 0L) {
            return 0L;
        }
        return accessTokenCreationTime.longValue() + expiresAfter;
    }

    public boolean isTokenExpired(GenericConfiguration configuration) {
        long tokenExpirationDate = getTokenExpirationDate(configuration);
        if (tokenExpirationDate <= 0L) {
            return false;
        }
        return System.currentTimeMillis() > tokenExpirationDate;
    }

    public String getTokenExpiryInfoString(GenericConfiguration configuration) {
        if (StringUtil.isEmpty(getAccessToken(configuration))) {
            return "no token exists";
        }
        long tokenExpirationDate = getTokenExpirationDate(configuration);
        if (tokenExpirationDate == 0L) {
            return "expires never";
        }
        String formattedExpirationDate = DateTimeUtil.formatDate("yyyy-MM-dd HH:mm", new Date(tokenExpirationDate));
        String timeDurationAppendix =
                " (" + StringUtil.formatTimeDuration(tokenExpirationDate - System.currentTimeMillis())
                        + ", Refreshable: " + isTokenRefreshSupported() + " )";
        if (isTokenExpired(configuration)) {
            return String.format("expired at %s", new Object[] { formattedExpirationDate }) + timeDurationAppendix;
        }
        return String.format("valid until %s", new Object[] { formattedExpirationDate }) + timeDurationAppendix;
    }

    public abstract List<String> getPropertiesWhichShouldntBePartOfApp();

    public abstract String getAuthorizationUrl(GenericConfiguration paramGenericConfiguration,
            DatameerServer paramDatameerServer,
            String paramString);

    public abstract String processCallback(GenericConfiguration paramGenericConfiguration,
            DatameerServer paramDatameerServer,
            CallbackRequest paramCallbackRequest);

    public static enum TokenRefresh {
        SUPPORTED,
        NOT_SUPPORTED;
    }
}
