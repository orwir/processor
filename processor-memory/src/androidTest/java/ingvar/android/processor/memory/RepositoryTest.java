package ingvar.android.processor.memory;

import junit.framework.TestCase;

import ingvar.android.processor.memory.persistence.BitmapMemoryRepository;
import ingvar.android.processor.memory.persistence.MemoryRepository;

/**
 * Created by Igor Zubenko on 2015.03.20.
 */
public class RepositoryTest extends TestCase {

    //private static final int
    private static final int MEGABYTE = 1024 * 1024;

    private MemoryRepository<String, String> memoryRepo;
    private MemoryRepository<String, String> decorationRepo;
    private BitmapMemoryRepository<String> bitmapRepo;

    public RepositoryTest() {
        memoryRepo = new MemoryRepository<>(3);
        decorationRepo = new MemoryRepository<String, String>(1, new MemoryRepository(3));
        bitmapRepo = new BitmapMemoryRepository<>(MEGABYTE);
    }

    public void testMemory() {
        assertTrue("Not implemented yet!", false);
    }

    public void testMemoryExpired() {
        assertTrue("Not implemented yet!", false);
    }

    public void testDecorated() {
        assertTrue("Not implemented yet!", false);
    }

    public void testBitmap() {
        assertTrue("Not implemented yet!", false);
    }

}
