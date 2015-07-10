package ingvar.android.processor.filesystem.test;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ServiceTestCase;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.filesystem.test.pojo.TestObject;
import ingvar.android.processor.filesystem.test.service.MockFilesystemService;
import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.Execution;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class ServiceTest extends ServiceTestCase<MockFilesystemService> {

    public ServiceTest() {
        super(MockFilesystemService.class);
    }

    public void testLoadObject() throws Exception {
        File file = new File(getContext().getCacheDir(), "load_test");
        TestObject object = new TestObject(2, "LoadTest", 10.42);
        getService().getFilesystemSource().save(file, object);

        Execution actual = getService().execute(new RequestFileTask(file));
        assertEquals(object, actual.getFuture().get());
    }

    public void testLoadBitmap() throws Exception {
        String image = "hexapod.png";
        Bitmap expected = BitmapFactory.decodeStream(getContext().getAssets().open(image));
        Execution actual = getService().execute(new RequestAssetBitmapTask(image));

        assertTrue(expected.sameAs(actual.<Bitmap>getFuture().get()));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bindService(new Intent(getContext(), MockFilesystemService.class));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(getService() != null) {
            getService().removeObservers(getContext());
            getService().clearCache();
        }
    }

    private class RequestFileTask extends SingleTask<File, TestObject, FilesystemSource> {

        public RequestFileTask(File key) {
            super(key, FilesystemSource.class);
        }

        @Override
        public TestObject process(IObserverManager observerManager, FilesystemSource source) {
            return source.load(getTaskKey());
        }
    }

    private class RequestAssetBitmapTask extends SingleTask<String, Bitmap, FilesystemSource> {

        public RequestAssetBitmapTask(String key) {
            super(key, FilesystemSource.class);
        }

        @Override
        public Bitmap process(IObserverManager observerManager, FilesystemSource source) {
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(source.getAssetManager().open(getTaskKey()));
                return BitmapFactory.decodeStream(stream);
            } catch (IOException e) {
                throw new ProcessorException(e);
            } finally {
                FileUtils.close(stream);
            }
        }

    }

    private class RequestAssetBitmapListTask extends SingleTask<String, List<Bitmap>, FilesystemSource> {

        public RequestAssetBitmapListTask(String key, long cacheExpirationTime) {
            super(key, String.class, FilesystemSource.class, cacheExpirationTime);
        }

        @Override
        public List<Bitmap> process(IObserverManager observerManager, FilesystemSource source) {
            List<Bitmap> result = new ArrayList<>();
            try {
                for(String name : source.getAssetManager().list(getTaskKey())) {
                    InputStream stream = null;
                    try {
                        stream = new BufferedInputStream(source.getAssetManager().open(name));
                        result.add(BitmapFactory.decodeStream(stream));
                    } catch (IOException e) {
                        throw new ProcessorException(e);
                    } finally {
                        FileUtils.close(stream);
                    }
                }
            } catch (IOException e) {
                return Collections.emptyList();
            }
            return result;
        }

    }

}
