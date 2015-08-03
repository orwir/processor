package ingvar.android.processor.test.util;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.AndroidTestCase;

import java.io.InputStream;

import ingvar.android.processor.util.PooledBitmapDecoder;

/**
 * Created by Igor Zubenko on 2015.07.11.
 */
public class PooledBitmapDecoderTest extends AndroidTestCase {

    public void testDecode() throws Exception {
        AssetManager assets = getContext().getAssets();
        InputStream stream = assets.open("hexapod.png");

        BitmapFactory.Options options = new BitmapFactory.Options();

        Bitmap bitmap = PooledBitmapDecoder.decode(stream, options, 0, 0);
        assertNotNull(bitmap);
        assertNull(options.inBitmap);
    }

    public void testCachedDecode() throws Exception {
        AssetManager assets = getContext().getAssets();
        InputStream stream1 = assets.open("hexapod.png");
        InputStream stream2 = assets.open("night.png");

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = PooledBitmapDecoder.decode(stream1, options, 0, 0);

        assertNotNull(bitmap);
        assertNull(options.inBitmap);

        PooledBitmapDecoder.free(bitmap);
        options = new BitmapFactory.Options();
        bitmap = PooledBitmapDecoder.decode(stream2, options, 0, 0);

        assertNotNull(bitmap);
        assertNotNull(options.inBitmap);
    }

}
