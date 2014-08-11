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

import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.kenshoo.integrations.plugins.connectors.util.OAuthUtil;

import datameer.dap.sdk.util.DatameerServer;

public class OAuth2SetupTest {

    private static final String serviceName = "GoogleCloudStorage";
    private static final String callbackUrlStr = "http://somehost:8080/callback";
    private static final String scope = "https://www.googleapis.com/auth/devstorage.read_only";
    private String authorizationUrl;

    private OAuth2Setup oAuth2Setup;
    private DatameerServer datameerServer;

    @Before
    public void prepareTest() throws Exception {
        URL callbackUrl;
        callbackUrl = new URL(callbackUrlStr);
        datameerServer = new DatameerServer(callbackUrl);
        oAuth2Setup = createOAuthSetup();
        authorizationUrl = createAuthorizationUrl();
    }

    @Test
    public void getAuthorizationUrlTest() {

        String authorizationUrl = oAuth2Setup.getAuthorizationUrl(null, datameerServer, serviceName);
        Assert.assertEquals(this.authorizationUrl, authorizationUrl);
    }

    public OAuth2Setup createOAuthSetup() {
        OAuth2Setup oAuth2Setup =
                new OAuth2Setup("google",
                        OAuthUtil.GOOGLE_AUTH_ENDPOINT,
                        OAuthUtil.GOOGLE_TOKEN_ENDPOINT,
                        OAuthSetup.TokenRefresh.SUPPORTED);
        oAuth2Setup.setOAuthClientData(createOAuthClientData());
        oAuth2Setup.setScope(scope);
        return oAuth2Setup;
    }

    public OAuthClientData createOAuthClientData() {
        OAuthClientData oAuthClientData = new OAuthClientData("test.client.id", "test.client.secret", callbackUrlStr);
        return oAuthClientData;
    }

    public String createAuthorizationUrl() throws Exception {
        String urlTemplate =
                "{0}?response_type=code&scope={1}&redirect_uri={2}&access_type=offline&approval_prompt=force&state={3}&client_id={4}";
        String state =
                URLEncoder.encode(datameerServer.getCallbackUrl().toString() + '?' + "service=" + serviceName, "utf-8");
        String encodedScope = URLEncoder.encode(scope, "utf-8");
        String redirectUrl = URLEncoder.encode(oAuth2Setup.getRedirectUrl(), "utf-8");
        String url =
                MessageFormat.format(urlTemplate,
                        OAuthUtil.GOOGLE_AUTH_ENDPOINT,
                        encodedScope,
                        redirectUrl,
                        state,
                        oAuth2Setup.getClientId());
        return url;
    }

}
