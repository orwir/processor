package ingvar.android.processor.sqlite.persistence;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.conversion.ConverterFactory;
import ingvar.android.literepo.util.CursorCommon;
import ingvar.android.processor.exception.PersistenceException;
import ingvar.android.processor.persistence.AbstractRepository;
import ingvar.android.processor.persistence.CompositeKey;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class SqliteRepository<R> extends AbstractRepository<SqlKey, R> {

    protected Converter<R> converter;
    protected Uri contentUri;
    protected Class<R> dataClass;
    private WeakReference<Context> contextRef;

    public SqliteRepository(Context context, Uri contentUri, Class<R> dataClass) {
        this.contextRef = new WeakReference<>(context);
        this.contentUri = contentUri;
        this.dataClass = dataClass;
        this.converter = provideConverter();
    }

    @Override
    protected <KEY> KEY composeKey(Object major, Object minor) {
        throw new UnsupportedOperationException("Not supported for SqliteRepository.");
    }

    @Override
    protected R persistSingle(SqlKey key, R data) {
        getContentResolver().insert(key.getUri(), converter.convert(data));
        getContentResolver().notifyChange(contentUri, null);
        return data;
    }

    @Override
    protected Collection<R> persistCollection(CompositeKey<SqlKey> key, Collection<R> data) {
        if(key.getMinors().size() > 0) {
            throw new PersistenceException("SqliteRepository doesn't support minor keys. CompositeKey just wrap SqlKey.");
        }

        int index = 0;
        ContentValues[] values = new ContentValues[data.size()];
        for(R item : data) {
            values[index++] = converter.convert(item);
        }
        SqlKey sql = key.getMajor();
        getContentResolver().bulkInsert(sql.getUri(), values);
        getContentResolver().notifyChange(contentUri, null);
        return data;
    }

    @Override
    protected R obtainSingle(SqlKey key, long expiryTime) {
        R result = null;
        if(isNotExpired(getCreationTime(key), expiryTime)) {
            Cursor cursor = getContentResolver().query(
                    key.getUri(),
                    key.getProjection(),
                    key.getSelection(),
                    key.getSelectionArgs(),
                    key.getSortOrder()
            );
            if(cursor.moveToFirst()) {
                result = converter.convert(cursor);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    protected Collection<R> obtainCollection(CompositeKey<SqlKey> key, long expiryTime) {
        Collection<R> result = new ArrayList<>();
        if(key.getMinors().size() > 0) {
            throw new PersistenceException("SqliteRepository doesn't support minor keys. CompositeKey just wrap SqlKey.");
        }

        SqlKey sql = key.getMajor();
        Cursor cursor = getContentResolver().query(
                sql.getUri(),
                sql.getProjection(),
                sql.getSelection(),
                sql.getSelectionArgs(),
                sql.getSortOrder()
        );
        while (cursor.moveToNext()) {
            if(isNotExpired(cursor, expiryTime)) {
                result.add(converter.convert(cursor));
            } else {
                //if one of items is expired return empty collection
                result.clear();
                break;
            }
        }
        cursor.close();
        return result;
    }

    @Override
    protected void removeSingle(SqlKey key) {
        getContentResolver().delete(key.getUri(), key.getSelection(), key.getSelectionArgs());
        getContentResolver().notifyChange(contentUri, null);
    }

    @Override
    protected void removeCollection(CompositeKey<SqlKey> key) {
        if(key.getMinors().size() > 0) {
            throw new PersistenceException("SqliteRepository doesn't support minor keys. CompositeKey just wrap SqlKey.");
        }
        removeSingle(key.getMajor());
    }

    @Override
    public void removeAll() {
        getContentResolver().delete(contentUri, null, null);
        getContentResolver().notifyChange(contentUri, null);
    }

    @Override
    public long getCreationTime(Object key) {
        long creationTime = -1;
        SqlKey sql = (SqlKey) key;

        Cursor cursor = getContentResolver()
                .query(sql.getUri(), new String[] {ExtendedColumns._CREATION_DATE},
                        sql.getSelection(), sql.getSelectionArgs(), sql.getSortOrder());

        if(cursor.moveToFirst()) {
            creationTime = CursorCommon.longv(cursor, ExtendedColumns._CREATION_DATE);
        }
        cursor.close();

        return creationTime;
    }

    @Override
    public boolean canHandle(Class dataClass) {
        return this.dataClass.equals(dataClass);
    }

    protected Converter<R> provideConverter() {
        return ConverterFactory.create(dataClass);
    }

    protected boolean isNotExpired(Cursor cursor, long expiryTime) {
        long creationTime = CursorCommon.longv(cursor, ExtendedColumns._CREATION_DATE);
        return isNotExpired(creationTime, expiryTime);
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
