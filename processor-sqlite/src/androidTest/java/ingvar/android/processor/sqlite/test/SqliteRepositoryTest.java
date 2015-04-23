package ingvar.android.processor.sqlite.test;

import android.net.Uri;
import android.test.ProviderTestCase2;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.persistence.SqlKey;
import ingvar.android.processor.sqlite.persistence.SqliteRepository;
import ingvar.android.processor.sqlite.test.db.TestContract;
import ingvar.android.processor.sqlite.test.db.TestProvider;
import ingvar.android.processor.sqlite.test.pojo.TestObject;

/**
 * Created by Igor Zubenko on 2015.04.22.
 */
public class SqliteRepositoryTest extends ProviderTestCase2<TestProvider> {

    private SqliteRepository<TestObject> repo;

    public SqliteRepositoryTest() {
        super(TestProvider.class, TestContract.AUTHORITY);
    }

    public void testCreate() {
        TestObject expected = new TestObject(null, "TestCreate", 10.2);
        repo.persist(new SqlKey(TestContract.Test.CONTENT_URI), expected);

        Uri uri = new UriBuilder()
            .authority(TestContract.AUTHORITY)
            .table(TestContract.Test.TABLE_NAME)
            .eq(TestContract.Test.Col.NAME, "TestCreate")
        .build();
        TestObject actual = repo.obtain(new SqlKey(uri, TestContract.Test.PROJECTION), Time.ALWAYS_RETURNED);

        repo.remove(new SqlKey(uri));

        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPrice(), actual.getPrice());
    }

    public void testRead() {
        Uri uri = new UriBuilder()
            .authority(TestContract.AUTHORITY)
            .table(TestContract.Test.TABLE_NAME)
            .eq(TestContract.Test.Col.NAME, "test1")
        .build();
        SqlKey key = new SqlKey(uri, TestContract.Test.PROJECTION);
        TestObject object = repo.obtain(key, Time.ALWAYS_RETURNED);

        assertNotNull(object);
    }

    public void testReadList() {
        Uri uri = new UriBuilder()
            .authority(TestContract.AUTHORITY)
            .table(TestContract.Test.TABLE_NAME)
            .eq(TestContract.Test.Col.NAME, "test1")
            .or().eq(TestContract.Test.Col.NAME, "test2")
        .build();
        SqlKey key = new SqlKey(uri, TestContract.Test.PROJECTION);
        /*List<TestObject> list = repo.obtainList(key, Time.ALWAYS_RETURNED);
        assertEquals(2, list.size());*/
    }

    public void testReadExpired() throws Exception {
        Uri uri = new UriBuilder()
                .authority(TestContract.AUTHORITY)
                .table(TestContract.Test.TABLE_NAME)
                .eq(TestContract.Test.Col.NAME, "test1")
                .build();
        SqlKey key = new SqlKey(uri, TestContract.Test.PROJECTION);
        Thread.sleep(80);
        TestObject object = repo.obtain(key, 60);

        assertNull(object);
    }

    public void testUpdate() {
        Uri getUri = new UriBuilder()
            .authority(TestContract.AUTHORITY)
            .table(TestContract.Test.TABLE_NAME)
            .eq(TestContract.Test.Col.NAME, "test1")
        .build();
        SqlKey getKey = new SqlKey(getUri, TestContract.Test.PROJECTION);

        TestObject expected = repo.obtain(getKey, Time.ALWAYS_RETURNED);
        assertNotNull(expected.getId());
        expected.setPrice(expected.getPrice() * 2);

        Uri updUri = new UriBuilder()
            .authority(TestContract.AUTHORITY)
            .table(TestContract.Test.TABLE_NAME)
            .eq(TestContract.Test.Col._ID, expected.getId())
        .build();
        repo.persist(new SqlKey(updUri), expected);

        TestObject actual = repo.obtain(getKey, Time.ALWAYS_RETURNED);

        assertEquals(expected.getPrice(), actual.getPrice());
    }

    public void testDelete() {
        TestObject expected = new TestObject(null, "TestCreate", 10.2);
        repo.persist(new SqlKey(TestContract.Test.CONTENT_URI), expected);

        Uri uri = new UriBuilder()
                .authority(TestContract.AUTHORITY)
                .table(TestContract.Test.TABLE_NAME)
                .eq(TestContract.Test.Col.NAME, "TestCreate")
                .build();
        SqlKey key = new SqlKey(uri, TestContract.Test.PROJECTION);
        repo.remove(key);

        TestObject actual = repo.obtain(key, Time.ALWAYS_RETURNED);
        assertNull("Data must be deleted!", actual);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        repo = new SqliteRepository<>(getMockContext(), TestContract.Test.CONTENT_URI, TestObject.class);
    }

}
