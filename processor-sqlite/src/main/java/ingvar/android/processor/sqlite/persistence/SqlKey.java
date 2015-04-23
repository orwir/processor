package ingvar.android.processor.sqlite.persistence;

import android.net.Uri;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class SqlKey {

    private Uri uri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;

    public SqlKey() {
        this(null, null, null, null, null);
    }

    public SqlKey(Uri uri) {
        this(uri, null, null, null, null);
    }

    public SqlKey(Uri uri, String[] projection) {
        this(uri, projection, null, null, null);
    }

    public SqlKey(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    public Uri getUri() {
        return uri;
    }

    public SqlKey setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public String[] getProjection() {
        return projection;
    }

    public SqlKey setProjection(String[] projection) {
        this.projection = projection;
        return this;
    }

    public String getSelection() {
        return selection;
    }

    public SqlKey setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public SqlKey setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public SqlKey setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

}
