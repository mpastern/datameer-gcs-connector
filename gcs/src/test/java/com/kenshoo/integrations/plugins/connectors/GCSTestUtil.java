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


package com.kenshoo.integrations.plugins.connectors;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.Storage.Objects;
import com.google.api.services.storage.Storage.Objects.Get;
import com.google.api.services.storage.model.StorageObject;
import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

public class GCSTestUtil {
    public final static String bucketName = "mybucket";
    public static List<StorageObject> bucketContent;

    public static Storage createStorageMock() {
        Storage storage = mock(Storage.class);
        try {
            when(storage.objects()).thenAnswer(new Answer<Storage.Objects>() {
                @Override
                public Objects answer(InvocationOnMock invocation) throws Throwable {
                    return createStorageObjectsMock();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return storage;
    }

    public static Storage.Objects createStorageObjectsMock() throws Exception {
        Objects objects = mock(Storage.Objects.class);
        when(objects.get(anyString(), anyString())).thenAnswer(new Answer<Storage.Objects.Get>() {
            @Override
            public Storage.Objects.Get answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String bucketName = (String) args[0];
                String objectName = (String) args[1];
                Storage.Objects.Get getObject = createStorageObjectsGetMock(bucketName, objectName);
                return getObject;
            }
        });
        when(objects.list(bucketName)).thenAnswer(new Answer<Storage.Objects.List>() {
            @Override
            public Storage.Objects.List answer(InvocationOnMock invocation) throws Throwable {
                return createStorageObjectsListMock();
            }
        });
        return objects;
    }

    public static Storage.Objects.List createStorageObjectsListMock() throws Exception {
        Storage.Objects.List list = mock(Storage.Objects.List.class);
        when(list.setPrefix(anyString())).thenAnswer(new Answer<Storage.Objects.List>() {
            @Override
            public Storage.Objects.List answer(InvocationOnMock invocation) {
                try {
                    invocation.callRealMethod();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return (Storage.Objects.List) invocation.getMock();
            }
        });
        when(list.getPrefix()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                String result = null;
                try {
                    result = (String) invocation.callRealMethod();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return result;
            }
        });
        when(list.execute()).thenAnswer(new Answer<com.google.api.services.storage.model.Objects>() {
            @Override
            public com.google.api.services.storage.model.Objects answer(InvocationOnMock invokation)
                    throws Throwable {
                Storage.Objects.List list = (Storage.Objects.List) invokation.getMock();
                String prefix = list.getPrefix();
                com.google.api.services.storage.model.Objects result =
                        createStorageObjects(prefix);
                return result;
            }
        });
        return list;
    }

    public static Storage.Objects.Get createStorageObjectsGetMock(String bucketName, String objectName)
            throws Exception {
        Storage.Objects.Get getObject = mock(Storage.Objects.Get.class);
        when(getObject.setObject(anyString())).thenAnswer(new Answer<Storage.Objects.Get>() {
            @Override
            public Get answer(InvocationOnMock invocation) throws Throwable {
                try {
                    invocation.callRealMethod();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return (Storage.Objects.Get) invocation.getMock();
            }
        });
        when(getObject.getObject()).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                String result = null;
                try {
                    result = (String) invocation.callRealMethod();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return result;
            }
        });
        when(getObject.execute()).thenAnswer(new Answer<StorageObject>() {
            @Override
            public StorageObject answer(InvocationOnMock invocation) throws Throwable {
                Storage.Objects.Get getObject = (Storage.Objects.Get) invocation.getMock();
                String objectName = getObject.getObject();
                List<StorageObject> list = filterBucketContent(objectName);
                if (list.size() > 0) {
                    return list.get(0);
                } else {
                    throw new FileNotFoundException(objectName);
                }
            }
        });
        when(getObject.executeMediaAsInputStream()).thenAnswer(new Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                Storage.Objects.Get getObject = (Storage.Objects.Get) invocation.getMock();
                String objectName = getObject.getObject();
                List<StorageObject> list = filterBucketContent(objectName);
                if (list.size() > 0) {
                    byte[] bytes = list.get(0).getName().getBytes();
                    return new ByteArrayInputStream(bytes, 0, bytes.length);
                } else {
                    throw new FileNotFoundException(objectName);
                }
            }
        });
        getObject.setObject(objectName);
        return getObject;
    }

    private static com.google.api.services.storage.model.Objects createStorageObjects(String prefix) {
        com.google.api.services.storage.model.Objects result =
                new com.google.api.services.storage.model.Objects();
        String globExpr = null;
        if (prefix == null) {
            globExpr = "*";
        } else {
            globExpr = prefix + "*";
        }
        List<StorageObject> list = filterBucketContent(globExpr);
        result.setItems(list);
        return result;
    }

    private static List<StorageObject> filterBucketContent(String globExpr) {
        List<StorageObject> result = new ArrayList<StorageObject>();
        for (StorageObject storageObject : bucketContent) {
            if (PathUtil.isGlobMatch(globExpr, storageObject.getName())) {
                result.add(storageObject);
            }
        }
        return result;
    }

    public static Path createBucketPath(String fileName) {
        String fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        String bucketStr = fsQuery.substring(0, fsQuery.indexOf(PathUtil.DEFAULT_ROOT));
        return new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + bucketStr);
    }

    public static Path createStorageObjectPath(String fileName) {
        String fsQuery = PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
        return new Path(PathUtil.GCSFS_DUMMY_HOST_PREFIX + fsQuery);
    }

    public static final String[] networkActivityFiles = {
            "NetworkActivity_5222_25673_05_03_2014.log.gz",
            "NetworkActivity_5222_25674_05_03_2014.log.gz",
            "NetworkActivity_5222_25673_05_04_2014.log.gz",
            "NetworkActivity_5222_25674_05_04_2014.log.gz",
    };

    public static final String[] clickActivityFiles = {
            "ClickActivity_5222_05_03_2014.log.gz",
            "ClickActivity_5222_05_03_2014.log.gz",
            "ClickActivity_5222_05_04_2014.log.gz"
    };

    public static final String[] otherFiles = {
            "TestData_5222_25673_05_08_2014.log.gz",
            "TestData_5222_25674_05_08_2014.log.gz"
    };

    static {
        bucketContent = new ArrayList<StorageObject>();
        for (String name : networkActivityFiles) {
            bucketContent.add(createStorageObject(name));
        }
        for (String name : clickActivityFiles) {
            bucketContent.add(createStorageObject(name));
        }
        for (String name : otherFiles) {
            bucketContent.add(createStorageObject(name));
        }
    }

    public static StorageObject createStorageObject(String name) {
        StorageObject storageObject = new StorageObject();
        storageObject.setName(name);
        storageObject.setSize(BigInteger.valueOf(name.getBytes().length));
        return storageObject;
    }

    public static class GCSConnectorMocked extends GCSConnector {
        @Override
        public Storage connectGCS(Configuration conf) {
            return createStorageMock();
        }

        @Override
        public void enableDirectDownload(Storage.Objects.Get getObject) {
        }
    }

    public static class GCSFileSystemMocked extends GCSFileSystem {
        @Override
        public GCSConnector connect(Path path) {
            GCSConnector gcsConnector = new GCSConnectorMocked();
            gcsConnector.connect(null, path);
            return gcsConnector;
        }
    }
}
