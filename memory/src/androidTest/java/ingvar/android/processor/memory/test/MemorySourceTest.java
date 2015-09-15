package ingvar.android.processor.memory.test;

import android.net.Uri;

import junit.framework.TestCase;

import ingvar.android.processor.memory.source.MemorySource;

/**
 * Created by Igor Zubenko on 2015.04.21.
 */
public class MemorySourceTest extends TestCase {

    public void testSize() {
        int overhead = 7;
        String _5b = "12345";
        String _9b = "123456789";

        MemorySource source = new MemorySource();
        source.put("5", _5b);
        assertEquals(5 + overhead, source.getSize());
        source.put("9", _9b);
        assertEquals(14 + overhead * source.getCount(), source.getSize());
    }

    public void testSizeNonSerializable() {
        Uri uri1 = Uri.parse("http://example.com");
        Uri uri2 = Uri.parse("http://another.com");

        MemorySource source = new MemorySource();
        source.put(1, uri1);
        source.put(2, uri2);

        assertEquals(0, source.getSize());
    }

}
