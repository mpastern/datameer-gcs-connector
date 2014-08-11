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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.NotImplementedException;
import org.apache.hadoop.fs.FSInputStream;
import org.apache.hadoop.fs.FileSystem;

public class SeekableInputStream extends FSInputStream {

    InputStream wrappedStream;
    FileSystem.Statistics stats;
    boolean closed;
    long pos;

    public SeekableInputStream(InputStream stream, FileSystem.Statistics stats) {
        if (stream == null) {
            throw new IllegalArgumentException("Null InputStream");
        }
        this.wrappedStream = stream;
        this.stats = stats;
        this.pos = 0L;
        this.closed = false;
    }

    @Override
    public long getPos() throws IOException {
        return pos;
    }

    @Override
    public synchronized int read() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }

        int byteRead = wrappedStream.read();
        if (byteRead >= 0) {
            pos += 1L;
        }
        if (((this.stats != null ? 1 : 0) & (byteRead >= 0 ? 1 : 0)) != 0) {
            stats.incrementBytesRead(1L);
        }
        return byteRead;
    }

    @Override
    public synchronized int read(byte[] buf, int off, int len) throws IOException {

        if (closed) {
            throw new IOException("Stream closed");
        }
        int result = wrappedStream.read(buf, off, len);
        if (result > 0) {
            pos += result;
        }
        if (((stats != null ? 1 : 0) & (result > 0 ? 1 : 0)) != 0) {
            stats.incrementBytesRead(result);
        }
        return result;
    }

    @Override
    public synchronized void close() throws IOException {
        closed = true;
        wrappedStream.close();
    }

    @Override
    public void mark(int readLimit) {
        throw new NotImplementedException("Mark is not supported");
    }

    @Override
    public void seek(long pos) throws IOException {
        throw new NotImplementedException("Seek not supported");
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        throw new NotImplementedException("Seek not supported");
    }
}
