package ingvar.android.processor.filesystem.test;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ApplicationTestCase;

import java.io.File;

import ingvar.android.processor.filesystem.persistence.BitmapFilesystemRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class BitmapFilesystemRepositoryTest extends ApplicationTestCase<Application> {

    private BitmapFilesystemRepository<String> repo;

    public BitmapFilesystemRepositoryTest() {
        super(Application.class);
    }

    public void testCache() throws Exception {
        final String image = "hexapod.png";
        Bitmap expected = BitmapFactory.decodeStream(getContext().getAssets().open(image));

        repo.persist(image, expected);
        Bitmap actual = repo.obtain(image, Time.ALWAYS_RETURNED);

        assertTrue(expected.sameAs(actual));
    }

    public void testOverflow() throws Exception {
        final String image1 = "hexapod.png";
        final String image2 = "deep_in_space.png";
        Bitmap expected1 = BitmapFactory.decodeStream(getContext().getAssets().open(image1));
        Bitmap expected2 = BitmapFactory.decodeStream(getContext().getAssets().open(image2));

        repo.persist(image1, expected1);
        Bitmap actual1 = repo.obtain(image1, Time.ALWAYS_RETURNED);
        assertTrue(expected1.sameAs(actual1));

        repo.persist(image2, expected2);
        Bitmap actual2 = repo.obtain(image2, Time.ALWAYS_RETURNED);
        assertTrue(expected2.sameAs(actual2));

        Thread.sleep(20);
        assertNull("Old file was not deleted", repo.obtain(image1, Time.ALWAYS_RETURNED));
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repo = new BitmapFilesystemRepository<>(new File(getContext().getCacheDir(), "test-cache"), 60 * 1024);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if(repo != null) {
            repo.removeAll();
        }
    }
}
