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


package com.kenshoo.integrations.plugins.connectors.util;

import static junit.framework.Assert.*;

import org.apache.hadoop.fs.Path;
import org.junit.Test;

public class PathUtilTest {

    private final String bucketName = "some.bucket";
    private final String fileName = "some.file.txt";

    @Test
    public void stripePathPrefixTest() {
        Path path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fileName);
        String result = PathUtil.stripePathPrefix(path);
        assertEquals(fileName, result);

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX + fileName);
        result = PathUtil.stripePathPrefix(path);
        assertEquals(fileName, result);

        path = new Path(fileName);
        result = PathUtil.stripePathPrefix(path);
        assertEquals(fileName, result);
    }

    @Test
    public void pathToStorageObjectNameTest() {
        Path path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + bucketName + PathUtil.DEFAULT_ROOT + fileName);
        String result = PathUtil.pathToStorageObjectName(path);
        assertEquals(fileName, result);

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX + bucketName + PathUtil.DEFAULT_ROOT + fileName);
        result = PathUtil.pathToStorageObjectName(path);
        assertEquals(fileName, result);

        path = new Path(bucketName + PathUtil.DEFAULT_ROOT + fileName);
        result = PathUtil.pathToStorageObjectName(path);
        assertEquals(fileName, result);

        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX);
        result = PathUtil.pathToStorageObjectName(path);
        assertEquals(null, result);

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX);
        result = PathUtil.pathToStorageObjectName(path);
        assertEquals(null, result);

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX + bucketName + PathUtil.DEFAULT_ROOT);
        result = PathUtil.pathToStorageObjectName(path);
        assertEquals(null, result);
    }

    @Test
    public void pathToBucketNameTest() {

        String fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        Path path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        String result = PathUtil.pathToBucketName(path);
        assertEquals(bucketName, result);

        path = new Path(PathUtil.GCSFS_SCHEMA_PREFIX + fsQuery);
        result = PathUtil.pathToBucketName(path);
        assertEquals(bucketName, result);

        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        result = PathUtil.pathToBucketName(path);
        assertEquals(bucketName, result);
    }

    @Test
    public void getFixedFilePrefixTest() {
        String result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(fileName, result);

        String fileName = "*wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(null, result);

        fileName = "?";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(null, result);

        String prefix = "aaa";
        fileName = prefix + "*wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(prefix, result);

        fileName = prefix + "%wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(prefix, result);

        fileName = prefix + "?wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(prefix, result);

        fileName = prefix + "[wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(prefix, result);

        fileName = prefix + "%wqwqqwqw";
        result = PathUtil.getFixedFilePrefix(fileName);
        assertEquals(prefix, result);
    }

    @Test
    public void pathIsBucketTest() {
        Path path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + bucketName + PathUtil.DEFAULT_ROOT + fileName);
        boolean result = PathUtil.pathIsBucket(path);
        assertEquals(false, result);

        path = new Path(bucketName + PathUtil.DEFAULT_ROOT + fileName);
        result = PathUtil.pathIsBucket(path);
        assertEquals(false, result);

        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + bucketName);
        result = PathUtil.pathIsBucket(path);
        assertEquals(true, result);

        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + bucketName + PathUtil.DEFAULT_ROOT);
        result = PathUtil.pathIsBucket(path);
        assertEquals(true, result);

        path = new Path(bucketName + PathUtil.DEFAULT_ROOT);
        result = PathUtil.pathIsBucket(path);
        assertEquals(true, result);

        path = new Path(bucketName);
        result = PathUtil.pathIsBucket(path);
        assertEquals(true, result);
    }

    @Test
    public void pathToFileNamePrefix() {
        String fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        Path path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        String prefix = PathUtil.getFixedFilePrefix(fileName);
        String result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        String fileName = "NetworkActivity_5222_*_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        fileName = "NetworkActivity_5222_*_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        fileName = "*NetworkActivity_5222_*_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        fileName = "*_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        fileName = "NetworkActivity_5222_2323232_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);

        fileName = "NetworkActivity_5222_?_%month%_%day%_%year%.log.gz";
        fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        path = new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
        prefix = PathUtil.getFixedFilePrefix(fileName);
        result = PathUtil.pathToFileNamePrefix(path);
        assertEquals(prefix, result);
    }

    @Test
    public void isBucketNameValid() {
        String bucketName = null;
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = ".";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "-";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "_";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "b";
        assertTrue(PathUtil.isBucketNameValid(bucketName));

        bucketName = "0";
        assertTrue(PathUtil.isBucketNameValid(bucketName));

        bucketName = "mybucket";
        assertTrue(PathUtil.isBucketNameValid(bucketName));

        bucketName = "my.buc-ke_t";
        assertTrue(PathUtil.isBucketNameValid(bucketName));

        bucketName = "0my.buc-ke_t0";
        assertTrue(PathUtil.isBucketNameValid(bucketName));

        bucketName = ".my.buc-ke_t0";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "-my.buc-ke_t0";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "_my.buc-ke_t0";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "my..buc-ke_t0";
        assertFalse(PathUtil.isBucketNameValid(bucketName));

        bucketName = "my.-buc-ke_t0";
        assertFalse(PathUtil.isBucketNameValid(bucketName));
    }

    @Test
    public void isGlobeMatchTest() {
        String globExpr = "aaa?bbb";
        String fileName = "aaa1bbb";

        boolean result = PathUtil.isGlobMatch(globExpr, fileName);
        assertTrue(result);

        fileName = "aaa11bbb";
        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertFalse(result);

        globExpr = "aaa*bbb";
        fileName = "aaa1bbb";

        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertTrue(result);

        fileName = "aaa11bbb";
        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertTrue(result);

        globExpr = "aaa[ab]bbb";

        fileName = "aaaabbb";
        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertTrue(result);

        fileName = "aaabbbb";
        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertTrue(result);

        fileName = "aaa1bbb";
        result = PathUtil.isGlobMatch(globExpr, fileName);
        assertFalse(result);
    }
}
