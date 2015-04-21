package ingvar.android.processor.filesystem.test;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

import java.io.File;

import ingvar.android.processor.filesystem.persistence.FilesystemRepository;
import ingvar.android.processor.filesystem.test.pojo.TestObject;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class FilesystemRepositoryTest extends ApplicationTestCase<Application> {

    private FilesystemRepository<Uri, TestObject> repo;

    public FilesystemRepositoryTest() {
        super(Application.class);
    }

    public void testCache() {
        Uri uri = Uri.parse("http://example.com/testfile");
        TestObject expected = new TestObject(1, "Test", 100.500);

        repo.persist(uri, expected);
        TestObject actual = repo.obtain(uri, Time.ALWAYS_RETURNED);
        assertEquals(expected, actual);
    }

    public void testExpired() throws Exception {
        Uri uri = Uri.parse("http://example.com/testfile");
        TestObject object = new TestObject(1, "Test", 100.500);

        repo.persist(uri, object);
        assertNotNull(repo.obtain(uri, Time.ALWAYS_RETURNED));

        Thread.sleep(150);
        assertNull(repo.obtain(uri, 100));
    }

    //overflow test in the BitmapFilesystemRepositoryTest.java

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repo = new FilesystemRepository<>(new File(getContext().getCacheDir(), "test-cache"), 50 * 1024);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(repo != null) {
            repo.removeAll();
        }
    }

}
