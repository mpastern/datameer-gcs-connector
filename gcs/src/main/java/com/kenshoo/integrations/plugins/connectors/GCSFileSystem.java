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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.storage.model.StorageObject;
import com.kenshoo.integrations.plugins.connectors.util.PathUtil;
import com.kenshoo.integrations.plugins.connectors.util.SeekableInputStream;

public class GCSFileSystem extends FileSystem {

    protected GCSConnector connect(Path path) {
        Configuration conf = getConf();
        GCSConnector gcsConnector = new GCSConnector();
        gcsConnector.connect(conf, path);
        return gcsConnector;
    }

    @Override
    public FileStatus[] listStatus(Path path) throws IOException {
        if (isFile(path)) {
            return new FileStatus[] { getFileStatus(path) };
        }
        GCSConnector gcsConnector = connect(path);
        List<StorageObject> storageObjects = gcsConnector.listBucketObjects();
        FileStatus[] fileStatus = new FileStatus[storageObjects.size()];
        int index = 0;
        for (StorageObject storageObject : storageObjects) {
            fileStatus[index] =
                    PathUtil.buildFileStatus(storageObject.getSize().longValue(),
                            false,
                            PathUtil.buildStorageObjectPath(path, storageObject.getName()));
            index++;
        }
        return fileStatus;
    }

    @Override
    public FileStatus getFileStatus(Path path) throws IOException {
        if (path.toString().equals(PathUtil.DEFAULT_ROOT) || path.toString().equals(PathUtil.GCSFS_DUMMY_HOST_PREFIX)
                || PathUtil.pathIsBucket(path)) {
            return PathUtil.buildFileStatus(-1L, true, path);
        }
        GCSConnector gcsConnector = connect(path);
        StorageObject storageObject = null;
        String storageObjectName = PathUtil.pathToStorageObjectName(path);

        if (storageObjectName == null) {
            return PathUtil.buildFileStatus(-1L, true, path);
        }
        try {
            storageObject = gcsConnector.getBucketObject(storageObjectName);
        } catch (GoogleJsonResponseException e) {
            throw new FileNotFoundException("File not found : " + path.toString());
        }
        return PathUtil.buildFileStatus(storageObject.getSize().longValue(), false, path);
    }

    @Override
    public FSDataInputStream open(Path path, int paramInt) throws IOException {
        InputStream is = null;
        GCSConnector gcsConnector = connect(path);
        String objectName = PathUtil.pathToStorageObjectName(path);
        is = gcsConnector.getInpuStream(objectName);
        FSDataInputStream fsIs = new FSDataInputStream(new SeekableInputStream(is, statistics));
        return fsIs;
    }

    @Override
    public URI getUri() {
        try {
            return new URI(GCSFileProtocol.FS_SCHEME,
                    null,
                    GCSFileProtocol.FS_DUMMY_HOST,
                    80,
                    PathUtil.DEFAULT_ROOT,
                    null,
                    null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public FSDataOutputStream create(Path path,
            FsPermission fsPermission, boolean paramBoolean, int paramInt,
            short paramShort, long paramLong, Progressable progressable)
            throws IOException {
        return null;
    }

    @Override
    public FSDataOutputStream append(Path path, int paramInt, Progressable progressable) throws IOException {
        return null;
    }

    @Override
    public boolean rename(Path path1, Path path2) throws IOException {
        return false;
    }

    @Override
    public boolean delete(Path path) throws IOException {
        return false;
    }

    @Override
    public boolean delete(Path path, boolean paramBoolean)
            throws IOException {
        return false;
    }

    @Override
    public void setWorkingDirectory(Path path) {
    }

    @Override
    public Path getWorkingDirectory() {
        return null;
    }

    @Override
    public boolean mkdirs(Path path, FsPermission fsPermission) throws IOException {
        return false;
    }

    @Override
    protected void checkPath(Path path) {
    }

    @Override
    public Path makeQualified(Path path) {
        return path;
    }

    @Override
    public void close() throws IOException {

    }
}
