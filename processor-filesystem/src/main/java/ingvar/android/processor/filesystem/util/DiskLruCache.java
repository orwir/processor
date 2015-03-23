/*
 * Copyright (C) 2015 Igor Zubenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ingvar.android.processor.filesystem.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ingvar.android.processor.util.BytesUtils;

public class DiskLruCache {

    public static enum CommitType {
        IMMEDIATELY,
        BY_UPDATES;
    }

    private static final String JOURNAL = "journal";
    private static final String JOURNAL_LOCK = "journal.lock";
    private static final FilenameFilter CACHE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            return !JOURNAL.equals(filename) && !JOURNAL_LOCK.equals(filename);
        }
    };

    private CommitType commitType;
    private int updatesBeforeCommit;
    private int updatesCount;
    private int maxSize;
    private final File root;
    private final File journal;
    private final Object asyncLock = new Object();
    private final ConcurrentLinkedQueue<String> lru;
    private final ExecutorService executor;


    public static DiskLruCache open(File directory, int maxBytes) {
        return open(directory, maxBytes, false);
    }

    /**
     * Create new disk lru cache instance
     *
     * @param directory - used to caching files
     * @param maxBytes - max cache size in bytes
     * @param useLock - prevent using cache folder to other instances (created with using lock)
     * @return - new instance
     */
    public static DiskLruCache open(File directory, int maxBytes, boolean useLock) {
        //create directory
        if(!directory.exists()) {
            directory.mkdir();
            if(!directory.exists()) {
                throw new IllegalStateException("Can't create cache directory!");
            }
        }
        //validate
        for(File file : directory.listFiles()) {
            if(file.isDirectory()) {
                throw new IllegalStateException("Subdirectories forbidden in the cache directory!");
            }
        }
        File lock = new File(directory, JOURNAL_LOCK);
        if(useLock && lock.exists()) {
            throw new IllegalStateException("Directory is locked by another instance!");
        }

        DiskLruCache cache = new DiskLruCache(directory, maxBytes);
        synchronized (DiskLruCache.class) {
            try {
                if(useLock && !lock.createNewFile()) {
                    throw new IllegalStateException("Can't create lock file");
                }
                cache.readJournal();
                cache.deleteUnknowns();
            } catch (IOException e) {
                lock.delete();
                throw new RuntimeException(e);
            } catch (RuntimeException e) {
                lock.delete();
                throw e;
            }
        }
        return cache;
    }

    private DiskLruCache(File directory, int maxBytes) {
        this.root = directory;
        this.maxSize = maxBytes;
        this.journal = new File(root, JOURNAL);
        this.lru = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newSingleThreadExecutor();
        this.commitType = CommitType.IMMEDIATELY;
        this.updatesBeforeCommit = 0;
        this.updatesCount = -1;
    }

    /**
     * Commit lru que to file and delete lock from directory.
     * After invoke this method don't use this cache instance.
     */
    public void close() {
        synchronized (asyncLock) {
            try {
                writeJournal();
            } catch (IOException e) {
                throw new IllegalStateException("Can't write data to journal", e);
            }
            File lock = new File(root, JOURNAL_LOCK);
            if (lock.exists() && !lock.delete()) {
                lock.deleteOnExit();
            }
        }
    }

    public void setCommitType(CommitType type, int updatesBeforeCommit) {
        this.commitType = type;
        this.updatesBeforeCommit = updatesBeforeCommit;
        this.updatesCount = 0;
    }

    /**
     * Get cached file
     * @param key
     * @return
     */
    public File getFile(String key) {
        File file = new File(root, key);
        if(file.exists() && file.isFile()) {
            updateKey(key);
            return file;
        }
        return null;
    }

    /**
     * Get cached serializable object from file
     * @param key
     * @param <T> - object type
     * @return
     */
    public <T> T get(String key) {
        Object result = null;
        File file = new File(root, key);
        if(file.exists() && file.isFile()) {
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(file));
                byte[] bytes = BytesUtils.streamToBytes(in);
                result = BytesUtils.fromBytes(bytes);
                updateKey(key);
                close(in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return (T) result;
    }

    /**
     * @param key
     * @return last modified date if file valid, else -1
     */
    public long getTime(String key) {
        File file = new File(root, key);
        if (file.exists() && file.isFile()) {
            return file.lastModified();
        }
        return -1;
    }

    public File[] getAll() {
        return root.listFiles(CACHE_FILTER);
    }

    public String[] getAllKeys() {
        return root.list(CACHE_FILTER);
    }

    public File put(String key, Serializable data) {
        File file = new File(root, key);
        synchronized (key.intern()) {
            try {
                OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false), 1024);
                out.write(BytesUtils.toBytes(data));
                updateKey(key);

                close(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        adjustCacheAsync();
        return file;
    }

    public boolean contains(String key) {
        return lru.contains(key) && new File(root, key).exists();
    }

    public void remove(String key) {
        File file = new File(root, key);
        file.delete();
        lru.remove(key);
        commit();
    }

    public void removeAll() {
        for (File file : getAll()) {
            file.delete();
            lru.remove(file.getName());
        }
        commit();
    }

    public int getSize() {
        int size = 0;
        for(File file : getAll()) {
            size += file.length();
        }
        return size;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int newSize) {
        boolean adjust = newSize < maxSize;
        maxSize = newSize;
        if(adjust) {
            adjustCacheAsync();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
//----------------------------------------------------------------------------------------------

    private void updateKey(String key) {
        lru.remove(key);
        lru.add(key);
        commit();
    }

    private void commit() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    switch (commitType) {
                        case IMMEDIATELY:
                            writeJournal();
                            break;
                        case BY_UPDATES:
                            updatesCount++;
                            if (updatesCount >= updatesBeforeCommit) {
                                updatesCount = 0;
                                writeJournal();
                            }
                            break;
                    }
                } catch (IOException ignored) {}
            }
        });
    }

    private void adjustCacheAsync() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                adjustCache();
            }
        });
    }

    private void adjustCache() {
        synchronized (asyncLock) {
            while (getSize() > maxSize) {
                String toEvict = lru.poll();
                if (toEvict == null) {
                    break;
                }
                remove(toEvict);
            }
        }
    }

    private void readJournal() throws IOException {
        if(!journal.exists()) {
            journal.createNewFile();
        } else {
            Scanner scanner = new Scanner(journal, "utf-8");
            while (scanner.hasNextLine()) {
                String key = scanner.nextLine();
                if (key != null && !key.isEmpty()) {
                    lru.add(key);
                }
            }
            scanner.close();
        }
    }

    private void writeJournal() throws IOException {
        Writer writer = new BufferedWriter(new FileWriter(journal), 1024);
        for (String key : lru) {
            writer.write(key);
            writer.write('\n');
        }
        close(writer);
    }

    private void deleteUnknowns() {
        for(File file : getAll()) {
            if(!lru.contains(file.getName())) {
                file.delete();
            }
        }
    }

    private void close(Closeable closeable) {
        try {
            if(closeable != null) {
                closeable.close();
            }
        } catch (IOException ignored) {}
    }

}
