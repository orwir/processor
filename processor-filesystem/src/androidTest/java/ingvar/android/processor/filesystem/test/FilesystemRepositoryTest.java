package ingvar.android.processor.filesystem.test;

import android.app.Application;
import android.net.Uri;
import android.test.ApplicationTestCase;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.filesystem.persistence.FilesystemRepository;
import ingvar.android.processor.filesystem.test.pojo.TestObject;
import ingvar.android.processor.persistence.CompositeKey;
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

    public void testCacheList() {
        CompositeKey<Uri> key = new CompositeKey<>(Uri.parse("http://example.com"),
            Uri.parse("http://example.com/testfile1"),
            Uri.parse("http://example.com/testfile2")
        );

        List<TestObject> expected = Arrays.asList(
                new TestObject(1, "Test1", 100.501),
                new TestObject(2, "Test2", 100.502)
        );

        repo.persist(key, expected);
        List<TestObject> actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(expected, actual);

        CompositeKey<Uri> key2 = new CompositeKey<>(Uri.parse("http://example.com"), Uri.parse("http://example.com/testfile1"));
        List<TestObject> actual2 = repo.obtain(key2, Time.ALWAYS_RETURNED);
        assertEquals(Arrays.asList(expected.get(0)), actual2);
    }

    public void testCacheListEmptyMajor() {
        CompositeKey<Uri> key = new CompositeKey<>(null,
                Uri.parse("http://example.com/testfile1"),
                Uri.parse("http://example.com/testfile2")
        );

        List<TestObject> expected = Arrays.asList(
                new TestObject(1, "Test1", 100.501),
                new TestObject(2, "Test2", 100.502)
        );

        repo.persist(key, expected);
        List<TestObject> actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertEquals(expected, actual);

        TestObject single = repo.obtain(Uri.parse("http://example.com/testfile1"), Time.ALWAYS_RETURNED);
        assertEquals(expected.get(0), single);
    }

    public void testExpired() throws Exception {
        Uri uri = Uri.parse("http://example.com/testfile");
        TestObject object = new TestObject(1, "Test", 100.500);

        repo.persist(uri, object);
        assertNotNull(repo.obtain(uri, Time.ALWAYS_RETURNED));

        Thread.sleep(150);
        assertNull(repo.obtain(uri, 100));
    }

    public void testOverflow() throws Exception {
        Uri uri1 = Uri.parse("content://example.com/test1");
        Uri uri2 = Uri.parse("content://example.com/test2");
        TestObject object1 = new TestObject(1, "name1", 11.2);
        TestObject object2 = new TestObject(2, "name2", 12.2);

        repo.persist(uri1, object1);
        TestObject actual1 = repo.obtain(uri1, Time.ALWAYS_RETURNED);
        assertEquals(object1, actual1);

        repo.persist(uri2, object2);
        TestObject actual2 = repo.obtain(uri2, Time.ALWAYS_RETURNED);
        assertEquals(object2, actual2);

        Thread.sleep(20);
        assertNull("Old file was not deleted", repo.obtain(uri1, Time.ALWAYS_RETURNED));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repo = new FilesystemRepository(new File(getContext().getCacheDir(), "test-cache"), 460);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(repo != null) {
            repo.removeAll();
        }
    }

}
