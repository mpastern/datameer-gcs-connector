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


package com.kenshoo.integrations.plugins.connectors.util;

import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuthClientData;

public class OAuthUtil {

    public static final String GOOGLE_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/auth";
    public static final String GOOGLE_TOKEN_ENDPOINT = "https://accounts.google.com/o/oauth2/token";

    public static GoogleClientSecrets createGoogleClientSecrets(OAuthClientData authClientData) {

        GoogleClientSecrets clientSecrets = new GoogleClientSecrets();

        Details web = new Details();
        web.setClientId(authClientData.getClientId());
        web.setClientSecret(authClientData.getSecret());
        web.setRedirectUris(Collections.singletonList(authClientData.getRedirectUri()));
        web.setAuthUri(GOOGLE_AUTH_ENDPOINT);
        web.setTokenUri(GOOGLE_TOKEN_ENDPOINT);
        clientSecrets.setWeb(web);

        return clientSecrets;
    }

}
