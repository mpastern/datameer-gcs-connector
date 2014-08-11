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

import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.bucketContent;
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.clickActivityFiles;
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.createBucketPath;
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.networkActivityFiles;
import com.kenshoo.integrations.plugins.connectors.GCSTestUtil.GCSConnectorMocked;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.hadoop.fs.Path;
import org.junit.Test;

import com.google.api.services.storage.model.StorageObject;

public class GCSConnectorTest {

    @Test
    public void listBucketObjectsTest() throws IOException {

        GCSConnector gcsConnector = new GCSConnectorMocked();
        Path path = createBucketPath("NetworkActivity_5222_2*_05_04_2014.log.gz");
        gcsConnector.connect(null, path);
        List<StorageObject> list = gcsConnector.listBucketObjects();
        Assert.assertEquals(networkActivityFiles.length, list.size());

        gcsConnector = new GCSConnectorMocked();
        path = createBucketPath("NetworkActivity_5222_2*_05_03_2014.log.gz");
        gcsConnector.connect(null, path);
        list = gcsConnector.listBucketObjects();
        Assert.assertEquals(networkActivityFiles.length, list.size());

        gcsConnector = new GCSConnectorMocked();
        path = createBucketPath("ClickActivity_5222_2*_05_03_2014.log.gz");
        gcsConnector.connect(null, path);
        list = gcsConnector.listBucketObjects();
        Assert.assertEquals(0, list.size());

        gcsConnector = new GCSConnectorMocked();
        path = createBucketPath("ClickActivity_5222_*_05_03_2014.log.gz");
        gcsConnector.connect(null, path);
        list = gcsConnector.listBucketObjects();
        Assert.assertEquals(clickActivityFiles.length, list.size());

        gcsConnector = new GCSConnectorMocked();
        path = createBucketPath("*");
        gcsConnector.connect(null, path);
        list = gcsConnector.listBucketObjects();
        Assert.assertEquals(bucketContent.size(), list.size());

        gcsConnector = new GCSConnectorMocked();
        path = createBucketPath("*_2014.log.gz");
        gcsConnector.connect(null, path);
        list = gcsConnector.listBucketObjects();
        Assert.assertEquals(bucketContent.size(), list.size());
    }
}
