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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

import com.google.api.client.util.Base64;
import com.kenshoo.integrations.plugins.connectors.GCSFileProtocol;

import datameer.dap.sdk.util.HadoopUtil;
import datameer.dap.sdk.util.StringUtil;

public class PathUtil {

    public static final String PATH_METADATA_DELIMITER = "|";
    public static final String PATH_FILE_PREFIX_SIGN = "@";
    public static final String PATH_BUCKET_SIGN = "&";
    public static final long DEFAULT_FILE_BLOCK_SIZE = 4096L;
    public static final String DEFAULT_ROOT = "/";
    public static final String GCSFS_DUMMY_HOST_PREFIX = GCSFileProtocol.FS_SCHEME + "://"
            + GCSFileProtocol.FS_DUMMY_HOST + DEFAULT_ROOT;
    public static final String GCSFS_SCHEMA_PREFIX = GCSFileProtocol.FS_SCHEME + "://";

    public static String stripePathPrefix(Path path) {
        String pathStr = path.toString();
        if (pathStr.startsWith(GCSFS_SCHEMA_PREFIX)) {
            if (pathStr.startsWith(GCSFS_DUMMY_HOST_PREFIX)) {
                pathStr = pathStr.substring(GCSFS_DUMMY_HOST_PREFIX.length());
            } else {
                pathStr = pathStr.substring(GCSFS_SCHEMA_PREFIX.length());
            }
        }
        return pathStr;
    }

    public static String pathToStorageObjectName(Path path) {
        String pathStr = stripePathPrefix(path);
        if (pathStr.contains(DEFAULT_ROOT)) {
            pathStr = pathStr.substring(pathStr.indexOf(DEFAULT_ROOT));
            if (pathStr.length() == DEFAULT_ROOT.length()) {
                pathStr = null;
            } else {
                pathStr = pathStr.substring(pathStr.indexOf(DEFAULT_ROOT) + DEFAULT_ROOT.length());
            }
        } else {
            pathStr = null;
        }
        return pathStr;
    }

    public static String pathToBucketName(Path path) {
        String query = pathToQuery(path);
        String bucketName = getToken(query, PATH_BUCKET_SIGN);
        return bucketName;
    }

    public static String pathToQuery(Path path) {
        String pathStr = stripePathPrefix(path);
        String query = null;
        if (pathStr.contains(DEFAULT_ROOT)) {
            query = pathStr.substring(0, pathStr.indexOf(DEFAULT_ROOT));
        } else {
            query = pathStr;
        }
        return query;
    }

    public static FileStatus buildFileStatus(long size, boolean isDir, Path path) {
        return new FileStatus(size, isDir, 1, DEFAULT_FILE_BLOCK_SIZE, 0L, 0L, null, null, null, path);
    }

    public static String pathToFileNamePrefix(Path path) {
        String query = pathToQuery(path);
        String fileNamePrefix = getToken(query, PATH_FILE_PREFIX_SIGN);
        return fileNamePrefix;
    }

    public static String getToken(String string, String tokenSign) {
        if (string == null || tokenSign == null) {
            return null;
        }
        String result = null;
        String[] tokens = string.split("\\" + PATH_METADATA_DELIMITER);
        for (String token : tokens) {
            if (token.startsWith(tokenSign)) {
                if (token.length() > tokenSign.length()) {
                    result = token.substring(token.indexOf(tokenSign) + tokenSign.length());
                }
                break;
            }
        }
        return result;
    }

    public static String getFixedFilePrefix(String fileName) {
        if (fileName == null) {
            return null;
        }
        String prefix = null;
        Pattern pattern = Pattern.compile("^([^\\*\\?\\{\\[%]+).*");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find() && matcher.groupCount() > 0) {
            prefix = matcher.group(1);
        }
        return prefix;
    }

    public static boolean pathIsBucket(Path path) {
        String pathStr = stripePathPrefix(path);
        if (pathStr.endsWith(DEFAULT_ROOT)) {
            pathStr = pathStr.substring(0, pathStr.lastIndexOf(DEFAULT_ROOT));
        }
        return !pathStr.contains(DEFAULT_ROOT);
    }

    public static Path buildStorageObjectPath(Path bucketPath, String storageObjectName) {
        String bucketPathStr = bucketPath.toString();
        if (!bucketPathStr.endsWith(DEFAULT_ROOT)) {
            bucketPathStr = bucketPathStr + DEFAULT_ROOT;
        }
        Path storageObjectPath = new Path(bucketPathStr + storageObjectName);
        return storageObjectPath;
    }

    public static String encodeNoGlobbing(String string) {
        String result;
        result = Base64.encodeBase64String(string.getBytes());
        result = result.replace('/', '-');
        return result;
    }

    public static String decodeNoGlobbing(String string) {
        String result;
        String tmpStr = string.replace('-', '/');
        result = new String(Base64.decodeBase64(tmpStr));
        return result;
    }

    public static boolean isGlobMatch(String globExpr, String string) {
        String regExp = HadoopUtil.convertGlobToRegex(globExpr);
        return string.matches(regExp);
    }

    public static String buildStorageObjectFileSystemQuery(String bucketName, String fileName) {
        StringBuilder queryPrefix = new StringBuilder();
        queryPrefix.append(PathUtil.PATH_BUCKET_SIGN + bucketName + PathUtil.PATH_METADATA_DELIMITER);
        String filePrefix = PathUtil.getFixedFilePrefix(fileName);
        if (!StringUtil.isEmpty(filePrefix)) {
            queryPrefix.append(PathUtil.PATH_FILE_PREFIX_SIGN + filePrefix);
        }
        return queryPrefix.toString() + PathUtil.DEFAULT_ROOT + fileName;
    }

    public static boolean isBucketNameValid(String bucketName) {
        boolean result = false;
        if (bucketName != null && bucketName.matches("[a-z0-9]|[a-z0-9][a-z0-9\\.\\-_]*[a-z0-9]")
                && !bucketName.matches(".*(\\.\\.|\\.\\-|\\-\\.).*")) {
            result = true;

        }
        return result;
    }
}
