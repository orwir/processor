package ingvar.android.processor.test.util;

import junit.framework.TestCase;

import ingvar.android.processor.util.CommonUtils;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class UtilsTest extends TestCase {

    public void testEquality() {
        Object vNull1 = null;
        Object vNull2 = null;
        Object vTest1 = "test";
        Object v5 = 5;
        Object vTest2 = "test";

        assertTrue(CommonUtils.isEquals(vNull1, vNull2));
        assertTrue(CommonUtils.isEquals(vTest1, vTest2));
        assertFalse(CommonUtils.isEquals(v5, vTest1));
        assertFalse(CommonUtils.isEquals(v5, vNull1));
    }

}
