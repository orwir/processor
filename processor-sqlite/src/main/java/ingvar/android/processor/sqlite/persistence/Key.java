package ingvar.android.processor.sqlite.persistence;

import android.net.Uri;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class Key {

    private Uri uri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;

    public Key() {
        this(null, null, null, null, null);
    }

    public Key(Uri uri) {
        this(uri, null, null, null, null);
    }

    public Key(Uri uri, String[] projection) {
        this(uri, projection, null, null, null);
    }

    public Key(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    public Uri getUri() {
        return uri;
    }

    public Key setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    public String[] getProjection() {
        return projection;
    }

    public Key setProjection(String[] projection) {
        this.projection = projection;
        return this;
    }

    public String getSelection() {
        return selection;
    }

    public Key setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    public Key setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public Key setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

}
