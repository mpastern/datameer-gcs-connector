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

public class OAuthClientData
{
    private final String clientId;
    private final String secret;
    private final String redirectUri;

    public OAuthClientData(String clientId, String secret, String redirectUri) {
        this.clientId = clientId;
        this.secret = secret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
