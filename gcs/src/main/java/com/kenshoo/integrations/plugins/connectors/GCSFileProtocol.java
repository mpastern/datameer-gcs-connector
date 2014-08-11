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


package com.kenshoo.integrations.plugins.connectors;

import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_REFRESH_TOKEN;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_RELAY_URL;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_CLIENT_ID;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_SECRET;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER;

import java.net.URI;
import java.util.List;

import org.apache.hadoop.conf.Configuration;

import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

import datameer.com.google.common.collect.Lists;
import datameer.dap.sdk.common.GenericConfiguration;
import datameer.dap.sdk.datastore.FileProtocol;
import datameer.dap.sdk.entity.DataStore;
import datameer.dap.sdk.property.PropertyGroupDefinition;

public class GCSFileProtocol extends FileProtocol {

    public static final long serialVersionUID = 1L;

    public static final String FS_DUMMY_HOST = "storage.google.com";
    public static final String FS_DUMMY_HOST_LABEL = "Host";
    public static final String FS_SCHEME = "gcsfs";
    public static final String FILE_SYSTEM_NAME = "Google Cloud Storage";

    public GCSFileProtocol() {
        super(FS_SCHEME, FS_DUMMY_HOST_LABEL, false);
    }

    @Override
    protected void addDetailsProperties(PropertyGroupDefinition connectionDetailsGroup) {
        addTransferProtocolProperty(connectionDetailsGroup);
    }

    @Override
    public List<URI> getUris(DataStore dataStore) {
        return Lists.newArrayList(new URI[] { newUri(getScheme(dataStore),
                null,
                FS_DUMMY_HOST,
                getPort(dataStore),
                PathUtil.DEFAULT_ROOT) });
    }

    @Override
    public void setupAuthentication(GenericConfiguration genericConfiguration,
            Configuration configuration,
            String arg1,
            String arg2) {
        configuration.setBoolean("fs.gcsfs.impl.disable.cache", true);
        configuration.setStrings("fs.gcsfs.impl", "com.kenshoo.integrations.plugins.connectors.GCSFileSystem");

        String accessToken = genericConfiguration.getStringProperty(PROPERTY_KEY_ACCESS_TOKEN, null);
        if (accessToken != null) {
            configuration.setStrings(PROPERTY_KEY_ACCESS_TOKEN, accessToken);
        }
        String refreshToken = genericConfiguration.getStringProperty(PROPERTY_KEY_REFRESH_TOKEN, null);
        if (refreshToken != null) {
            configuration.setStrings(PROPERTY_KEY_REFRESH_TOKEN, refreshToken);
        }
        Long accessTokenCreationTime =
                genericConfiguration.getLongProperty(PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME, null);
        if (accessTokenCreationTime != null) {
            configuration.setLong(PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME, accessTokenCreationTime);
        }
        Long accessTokenExpirationAfter =
                genericConfiguration.getLongProperty(PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER, null);
        if (accessTokenExpirationAfter != null) {
            configuration.setLong(PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER, accessTokenExpirationAfter);
        }
        String relayURL = genericConfiguration.getStringProperty(PROPERTY_KEY_OAUTH_RELAY_URL, null);
        if (relayURL != null) {
            configuration.setStrings(PROPERTY_KEY_OAUTH_RELAY_URL, relayURL);
        }
        String clientId = genericConfiguration.getStringProperty(PROPERTY_KEY_OAUTH_CLIENT_ID, null);
        if (clientId != null) {
            configuration.setStrings(PROPERTY_KEY_OAUTH_CLIENT_ID, clientId);
        }
        String secret = genericConfiguration.getStringProperty(PROPERTY_KEY_OAUTH_SECRET, null);
        if (secret != null) {
            configuration.setStrings(PROPERTY_KEY_OAUTH_SECRET, secret);
        }
    }

    @Override
    public String getName() {
        return FILE_SYSTEM_NAME;
    }
}
