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
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.createBucketPath;
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.createStorageObjectPath;
import static com.kenshoo.integrations.plugins.connectors.GCSTestUtil.networkActivityFiles;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import com.kenshoo.integrations.plugins.connectors.GCSTestUtil.GCSFileSystemMocked;
import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

public class GCSFileSystemTest {

    @Test
    public void listStatusTest() throws IOException {
        Path path = createBucketPath("");
        GCSFileSystem gcsFileSystem = new GCSFileSystemMocked();
        FileStatus[] fileStatuses = gcsFileSystem.listStatus(path);
        assertNotNull(fileStatuses);
        assertEquals(bucketContent.size(), fileStatuses.length);

        path = createBucketPath("NetworkActivity_5222_2*_05_04_2014.log.gz");
        gcsFileSystem = new GCSFileSystemMocked();
        fileStatuses = gcsFileSystem.listStatus(path);
        assertNotNull(fileStatuses);
        assertEquals(networkActivityFiles.length, fileStatuses.length);

        Set<Path> resultPathSet = new HashSet<Path>();
        for (FileStatus fileStatus : fileStatuses) {
            resultPathSet.add(fileStatus.getPath());
        }
        for (String fileName : networkActivityFiles) {
            Path filePath = PathUtil.buildStorageObjectPath(path, fileName);
            assertTrue(resultPathSet.contains(filePath));
        }

        path = createStorageObjectPath("NetworkActivity_5222_25673_05_03_2014.log.gz");
        gcsFileSystem = new GCSFileSystemMocked();
        fileStatuses = gcsFileSystem.listStatus(path);
        assertNotNull(fileStatuses);
        assertTrue(fileStatuses.length == 1);
        assertEquals(path, fileStatuses[0].getPath());
    }

    @Test
    public void getFileStatusTest() throws IOException {
        Path path = createBucketPath("");
        GCSFileSystem gcsFileSystem = new GCSFileSystemMocked();
        FileStatus fileStatus = gcsFileSystem.getFileStatus(path);
        assertNotNull(fileStatus);
        assertTrue(fileStatus.isDir());

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX);
        gcsFileSystem = new GCSFileSystemMocked();
        fileStatus = gcsFileSystem.getFileStatus(path);
        assertNotNull(fileStatus);
        assertTrue(fileStatus.isDir());

        path = createStorageObjectPath("NetworkActivity_5222_25673_05_03_2014.log.gz");
        gcsFileSystem = new GCSFileSystemMocked();
        fileStatus = gcsFileSystem.getFileStatus(path);
        assertNotNull(fileStatus);
        assertFalse(fileStatus.isDir());
        assertEquals(path, fileStatus.getPath());
    }

    @Test(expected = FileNotFoundException.class)
    public void getFileStatusNonExistingFileNegativeTest() throws IOException {
        Path path =
                createStorageObjectPath("NonExisting_5222_25673_05_03_2014.log.gz");
        GCSFileSystem gcsFileSystem = new GCSFileSystemMocked();
        gcsFileSystem.getFileStatus(path);
    }

    @Test
    public void openTest() throws IOException {
        String fileName = "NetworkActivity_5222_25673_05_03_2014.log.gz";
        Path path = createStorageObjectPath(fileName);
        GCSFileSystem gcsFileSystem = new GCSFileSystemMocked();
        InputStream is = gcsFileSystem.open(path, 4096);
        byte[] bytes = new byte[fileName.getBytes().length];
        is.read(bytes);
        assertEquals(fileName, new String(bytes));
    }

    @Test(expected = FileNotFoundException.class)
    public void openIncorrectFileNameNegativeTest() throws IOException {
        String fileName = "NonExisting_5222_25673_05_03_2014.log.gz";
        Path path = createStorageObjectPath(fileName);
        GCSFileSystem gcsFileSystem = new GCSFileSystemMocked();
        gcsFileSystem.open(path, 4096);
    }
}
