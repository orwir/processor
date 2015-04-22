package ingvar.android.processor.sqlite.persistence;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.conversion.ConverterFactory;
import ingvar.android.literepo.util.CursorCommon;
import ingvar.android.processor.persistence.IRepository;
import ingvar.android.processor.persistence.Time;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class SqliteRepository<R> implements IRepository<Key, R> {

    protected Converter<R> converter;
    protected Uri contentUri;
    protected Class dataClass;
    private WeakReference<Context> contextRef;

    public SqliteRepository(Context context, Uri contentUri, Class dataClass) {
        this.contextRef = new WeakReference<>(context);
        this.contentUri = contentUri;
        this.dataClass = dataClass;
        this.converter = provideConverter();
    }

    @Override
    public R persist(Key key, R data) {
        getContentResolver().insert(key.getUri(), converter.convert(data));
        getContentResolver().notifyChange(contentUri, null);
        return data;
    }

    @Override
    public R obtain(Key key, long expiryTime) {
        R result = null;
        if(isNotExpired(key, expiryTime)) {
            Cursor cursor = getContentResolver().query(
                    key.getUri(),
                    key.getProjection(),
                    key.getSelection(),
                    key.getSelectionArgs(),
                    key.getSortOrder());
            if(cursor.moveToFirst()) {
                result = converter.convert(cursor);
            }
            cursor.close();
        }
        return result;
    }

    /*TODO:
    public List<R> obtainList(Key key, long expiryTime) {
        List<R> result = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                key.getUri(),
                key.getProjection(),
                key.getSelection(),
                key.getSelectionArgs(),
                key.getSortOrder());
        while (cursor.moveToNext()) {
            if(isNotExpired(cursor, expiryTime)) {
                result.add(converter.convert(cursor));
            }
        }
        cursor.close();
        return result;
    }*/

    @Override
    public long getCreationTime(Key key) {
        long creationTime = -1;

        Cursor cursor = getContentResolver()
                .query(key.getUri(), new String[] {ExtendedColumns._CREATION_DATE},
                        key.getSelection(), key.getSelectionArgs(), key.getSortOrder());

        if(cursor.moveToFirst()) {
            creationTime = CursorCommon.longv(cursor, ExtendedColumns._CREATION_DATE);
        }
        cursor.close();

        return creationTime;
    }

    @Override
    public void remove(Key key) {
        getContentResolver().delete(key.getUri(), key.getSelection(), key.getSelectionArgs());
        getContentResolver().notifyChange(contentUri, null);
    }

    @Override
    public void removeAll() {
        getContentResolver().delete(contentUri, null, null);
        getContentResolver().notifyChange(contentUri, null);
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return this.dataClass.equals(dataClass);
    }

    protected Converter<R> provideConverter() {
        return ConverterFactory.create(dataClass);
    }

    protected boolean isNotExpired(Key key, long expiryTime) {
        long creationTime = getCreationTime(key);
        return creationTime >= 0
            && (expiryTime == Time.ALWAYS_RETURNED
            || System.currentTimeMillis() - expiryTime <= creationTime);
    }

    protected boolean isNotExpired(Cursor cursor, long expiryTime) {
        long creationTime = CursorCommon.longv(cursor, ExtendedColumns._CREATION_DATE);
        return creationTime >= 0
                && (expiryTime == Time.ALWAYS_RETURNED
                || System.currentTimeMillis() - expiryTime <= creationTime);
    }

    protected Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

    protected ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

}
