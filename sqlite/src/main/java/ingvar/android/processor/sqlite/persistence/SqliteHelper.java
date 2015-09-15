package ingvar.android.processor.sqlite.persistence;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedInputStream;
import java.lang.ref.WeakReference;

import ingvar.android.processor.util.CommonUtils;

/**
 * Created by Igor Zubenko on 2015.06.27.
 */
public abstract class SqliteHelper extends SQLiteOpenHelper {

    private WeakReference<Context> contextRef;
    private boolean enableForeignKeys;

    public SqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        contextRef = new WeakReference<>(context);
        enableForeignKeys = false;
    }

    public SqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        contextRef = new WeakReference<>(context);
        enableForeignKeys = false;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if(!db.isReadOnly() && enableForeignKeys) {
            db.execSQL("PRAGMA foreign_keys = ON");
        }
    }

    public void setEnableForeignKeys(boolean enable) {
        enableForeignKeys = enable;
    }

    protected Context getContext() {
        return CommonUtils.getReference(contextRef);
    }

    protected String extractSql(AssetManager manager, String assetPath) {
        BufferedInputStream is = null;
        try {
            StringBuilder sb = new StringBuilder();
            is = new BufferedInputStream(manager.open(assetPath));
            int read;
            while ((read = is.read()) != -1) {
                sb.append((char) read);
            }
            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {
            if(is != null) {try {is.close();} catch (Exception ignored) {}}
        }
    }

}
