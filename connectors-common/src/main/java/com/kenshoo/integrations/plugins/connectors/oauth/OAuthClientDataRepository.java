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

import com.kenshoo.integrations.plugins.connectors.util.KenshooProperties;

public class OAuthClientDataRepository
{
    static {
        String clientId = KenshooProperties.getProperty("oauth.data.google.clientid");
        String redirectUri = KenshooProperties.getProperty("oauth.data.google.redirecturi");
        String secret = KenshooProperties.getProperty("oauth.data.google.secret");

        GOOGLE_CLIENT_DATA = new OAuthClientData(clientId, secret, redirectUri);
    }

    public static OAuthClientData GOOGLE_CLIENT_DATA;
}
