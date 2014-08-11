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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.amber.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.apache.log4j.Logger;

import datameer.com.google.common.collect.ImmutableList;
import datameer.dap.sdk.common.GenericConfiguration;
import datameer.dap.sdk.property.CallbackRequest;
import datameer.dap.sdk.util.DatameerServer;
import datameer.dap.sdk.util.ExceptionUtil;
import datameer.dap.sdk.util.StringUtil;
import datameer.dap.sdk.util.UriUtil;

public class OAuth2Setup extends OAuthSetup {
    private static final Logger LOG = Logger.getLogger(OAuth2Authentication.class);
    public static final String PROPERTY_KEY_REFRESH_TOKEN = "authentication.oauth.refreshtoken";
    public static final String PROPERTY_KEY_OAUTH_RELAY_URL = "authentication.oauth.relay.url";
    public static final String PROPERTY_KEY_OAUTH_CLIENT_ID = "authentication.oauth.clientid";
    public static final String PROPERTY_KEY_OAUTH_SECRET = "authentication.oauth.secret";
    private final String authorizationUrl;
    private final String tokenUrl;
    private Class<? extends OAuthAccessTokenResponse> tokenResponseClass = OAuthJSONAccessTokenResponse.class;

    public OAuth2Setup(String oauthServiceName,
            String authorizationUrl,
            String tokenUrl,
            OAuthSetup.TokenRefresh tokenRefresh) {
        super(oauthServiceName, tokenRefresh);
        this.authorizationUrl = authorizationUrl;
        this.tokenUrl = tokenUrl;
    }

    public String getAuthorizationUrl() {
        return this.authorizationUrl;
    }

    public String getTokenUrl() {
        return this.tokenUrl;
    }

    public Class<? extends OAuthAccessTokenResponse> getTokenResponseClass() {
        return this.tokenResponseClass;
    }

    public void setTokenResponseClass(Class<? extends OAuthAccessTokenResponse> tokenResponseClass) {
        this.tokenResponseClass = tokenResponseClass;
    }

    public void handleTokenResponse(OAuthAccessTokenResponse tokenResponse, GenericConfiguration configuration) {
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();

        Long expiresAfterInSeconds = tokenResponse.getExpiresIn();
        configuration.setStringProperty(PROPERTY_KEY_ACCESS_TOKEN, accessToken);
        configuration.setLongProperty(PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME, Long.valueOf(System.currentTimeMillis()));
        if (refreshToken != null) {
            configuration.setStringProperty(PROPERTY_KEY_REFRESH_TOKEN, refreshToken);
        }
        if (expiresAfterInSeconds != null)
            configuration.setLongProperty(PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER,
                    Long.valueOf(TimeUnit.SECONDS.toMillis(expiresAfterInSeconds.longValue())));

        String relayURL = getRedirectUrl();
        configuration.setStringProperty(PROPERTY_KEY_OAUTH_RELAY_URL, relayURL);
        String clientId = getClientId();
        configuration.setStringProperty(PROPERTY_KEY_OAUTH_CLIENT_ID, clientId);
        String secret = getClientSecret();
        configuration.setStringProperty(PROPERTY_KEY_OAUTH_SECRET, secret);
    }

    @Override
    public List<String> getPropertiesWhichShouldntBePartOfApp() {
        return ImmutableList.of(PROPERTY_KEY_ACCESS_TOKEN,
                PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME,
                PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER,
                PROPERTY_KEY_REFRESH_TOKEN,
                OAuthAuthentication.PROPERTY_KEY_SERVICE_OAUTH_TOKEN_INFO);
    }

    @Override
    public String getAuthorizationUrl(GenericConfiguration configuration,
            DatameerServer datameerServer,
            String serviceName) {
        OAuthClientRequest.AuthenticationRequestBuilder builder =
                OAuthClientRequest.authorizationLocation(getAuthorizationUrl());
        builder.setClientId(getClientId());
        builder.setScope(getScope());
        builder.setResponseType("code");
        builder.setParameter("access_type", "offline");

        builder.setParameter("approval_prompt", "force");
        builder.setState(createStateParameter(datameerServer, serviceName));
        builder.setRedirectURI(getRedirectUrl());
        try {
            OAuthClientRequest request = builder.buildQueryMessage();
            return request.getLocationUri();
        } catch (OAuthSystemException e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
    }

    private String createStateParameter(DatameerServer datameerServer, String service) {
        String url = datameerServer.getCallbackUrl().toString();
        String encodedService = null;
        try {
            encodedService = URLEncoder.encode(service, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return url + '?' + "service=" + encodedService;
    }

    @Override
    public String processCallback(GenericConfiguration configuration,
            DatameerServer datameerServer,
            CallbackRequest request) {
        String error = request.getParameter("error");
        if (!StringUtil.isEmpty(error)) {
            if (error.equals("access_denied")) {
                return "You have to grant the permissions on the external authentication page in order to proceed!";
            }
            return "Requested permissions not granted: " + error;
        }
        String code = request.getParameter("code");
        code = UriUtil.decodeQueryParam(code);
        try {
            OAuthAccessTokenResponse tokenResponse = fetchAccessToken(configuration, datameerServer, code);
            handleTokenResponse(tokenResponse, configuration);
        } catch (Exception e) {
            LOG.error("Could not get acccess token.", e);
            return "Could not get access-token for code '" + code + "': " + e.getMessage();
        }

        configuration.setStringProperty(OAuthAuthentication.PROPERTY_KEY_SERVICE_OAUTH_TOKEN_INFO,
                getTokenExpiryInfoString(configuration));
        return null;
    }

    private OAuthAccessTokenResponse fetchAccessToken(GenericConfiguration configuration,
            DatameerServer datameerServer,
            String code)
            throws OAuthSystemException, OAuthProblemException {
        OAuthClientRequest.TokenRequestBuilder builder = OAuthClientRequest.tokenLocation(getTokenUrl());
        builder.setGrantType(GrantType.AUTHORIZATION_CODE);
        builder.setClientId(getClientId());
        builder.setClientSecret(getClientSecret());
        builder.setRedirectURI(getRedirectUrl());
        builder.setCode(code);

        OAuthClientRequest request = builder.buildBodyMessage();
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        return oAuthClient.accessToken(request, getTokenResponseClass());
    }

    public static OAuth2Setup create(OAuth2BasedDataStoreType dataStoreType,
            String authorizationUrl,
            String tokenUrl,
            OAuthSetup.TokenRefresh tokenRefresh) {
        return new OAuth2Setup(dataStoreType.getProviderName(), authorizationUrl, tokenUrl, tokenRefresh);
    }
}
