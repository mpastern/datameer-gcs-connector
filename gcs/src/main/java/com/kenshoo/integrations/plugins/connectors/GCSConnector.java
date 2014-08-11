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

import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_CLIENT_ID;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_RELAY_URL;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_OAUTH_SECRET;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup.PROPERTY_KEY_REFRESH_TOKEN;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME;
import static com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup.PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuthClientData;
import com.kenshoo.integrations.plugins.connectors.util.OAuthUtil;
import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

public class GCSConnector {
    protected String bucketName;
    protected String objectNamePrefix;
    protected Storage googleCloudStorage;

    public void connect(Configuration conf, Path path) {
        bucketName = PathUtil.pathToBucketName(path);
        objectNamePrefix = PathUtil.pathToFileNamePrefix(path);
        googleCloudStorage = connectGCS(conf);
    }

    public Storage connectGCS(Configuration conf) {
        HttpTransport httpTransport;
        JsonFactory jsonFactory;

        jsonFactory = JacksonFactory.getDefaultInstance();
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        GoogleClientSecrets clientSecrets = buildGoogleClientSecrets(conf);

        TokenResponse tokenResponse = buildTokenResponse(conf);

        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(jsonFactory)
                .setClientSecrets(clientSecrets)
                .build()
                .setFromTokenResponse(tokenResponse);

        return new Storage.Builder(httpTransport, jsonFactory, null)
                .setHttpRequestInitializer(credential)
                .build();
    }

    public GoogleClientSecrets buildGoogleClientSecrets(Configuration conf) {
        String clientId = conf.get(PROPERTY_KEY_OAUTH_CLIENT_ID, null);
        String secret = conf.get(PROPERTY_KEY_OAUTH_SECRET, null);
        String redirectUrl = conf.get(PROPERTY_KEY_OAUTH_RELAY_URL, null);
        OAuthClientData oAuthClientData = new OAuthClientData(clientId, secret, redirectUrl);
        return OAuthUtil.createGoogleClientSecrets(oAuthClientData);
    }

    public TokenResponse buildTokenResponse(Configuration conf) {
        long expiresInSeconds = getExpireInSeconds(conf);

        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken(conf.get(PROPERTY_KEY_ACCESS_TOKEN, null));
        tokenResponse.setRefreshToken(conf.get(PROPERTY_KEY_REFRESH_TOKEN, null));
        tokenResponse.setScope(StorageScopes.DEVSTORAGE_READ_ONLY);
        tokenResponse.setExpiresInSeconds(expiresInSeconds);
        return tokenResponse;
    }

    public long getExpireInSeconds(Configuration conf) {
        long expirationDate = getTokenExpirationDate(conf);
        return (expirationDate - System.currentTimeMillis()) / 1000L;
    }

    public long getTokenExpirationDate(Configuration conf) {
        long accessTokenCreationTime = conf.getLong(PROPERTY_KEY_ACCESS_TOKEN_CREATION_TIME, 0L);
        if (accessTokenCreationTime == 0L) {
            return 0L;
        }
        long expiresAfter = conf.getLong(PROPERTY_KEY_ACCESS_TOKEN_EXPIRES_AFTER, 0L);
        if (expiresAfter == 0L) {
            return 0L;
        }
        return accessTokenCreationTime + expiresAfter;
    }

    public List<StorageObject> listBucketObjects() throws IOException {
        return listBucketObjects(objectNamePrefix);
    }

    public List<StorageObject> listBucketObjects(String prefix) throws IOException {
        Storage.Objects.List listObjects = googleCloudStorage.objects().list(bucketName).setPrefix(prefix);
        Objects objects = listObjects.execute();
        List<StorageObject> storageObjects = new ArrayList<StorageObject>();
        while (objects.getItems() != null && !objects.getItems().isEmpty()) {
            storageObjects.addAll(objects.getItems());
            String nextPageToken = objects.getNextPageToken();
            if (nextPageToken == null) {
                break;
            }
            listObjects.setPageToken(nextPageToken);
            objects = listObjects.execute();
        }
        return storageObjects;
    }

    public StorageObject getBucketObject(String objectName) throws IOException {
        Storage.Objects.Get getObject = googleCloudStorage.objects().get(bucketName, objectName);
        StorageObject object = getObject.execute();
        return object;
    }

    public InputStream getInpuStream(String objectName) throws IOException {
        Storage.Objects.Get getObject = googleCloudStorage.objects().get(bucketName, objectName);
        enableDirectDownload(getObject);
        InputStream is = getObject.executeMediaAsInputStream();
        return is;
    }

    public void enableDirectDownload(Storage.Objects.Get getObject) {
        getObject.getMediaHttpDownloader().setDirectDownloadEnabled(true);
    }
}
