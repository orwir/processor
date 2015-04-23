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

    @Override
    public int hashCode() {
        int hashCode = 31;
        if(uri != null) {
            hashCode *= 31 + uri.hashCode();
        }
        if(projection != null) {
            hashCode *= 31 + projection.hashCode();
        }
        if(selection != null) {
            hashCode *= 31 + selection.hashCode();
        }
        //selection args not active
        if(sortOrder != null) {
            hashCode *= 31 + sortOrder.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof SqlKey) {
            SqlKey o = (SqlKey) obj;
            return isEquals(uri, o.uri)
                && isEquals(projection, o.projection)
                && isEquals(selection, o.selection)
                //selection args not active
                && isEquals(sortOrder, o.sortOrder);
        }
        return false;
    }

    private boolean isEquals(Object o1, Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
    }

}
