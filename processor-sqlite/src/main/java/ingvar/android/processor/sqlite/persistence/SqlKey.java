package ingvar.android.processor.sqlite.persistence;

import android.net.Uri;

import static ingvar.android.processor.util.CommonUtils.isEquals;
import static ingvar.android.processor.util.CommonUtils.objectHashCode;

/**
 * Identifier for all sql-requests.
 * Used with {@link SqliteRepository}.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.04.07.
 */
public class SqlKey {

    private Uri uri;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String sortOrder;
    private String columnCreationDate;

    public SqlKey() {
        this(null, null, null, null, null);
    }

    public SqlKey(Uri uri) {
        this(uri, null, null, null, null);
    }

    public SqlKey(Uri uri, String[] projection) {
        this(uri, projection, null, null, null);
    }

    /**
     * Create new sql key.
     *
     * @param uri uri to content provider
     * @param projection projection of columns
     * @param selection query selection
     * @param selectionArgs query selection args
     * @param sortOrder sort order
     */
    public SqlKey(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.sortOrder = sortOrder;
    }

    /**
     * Get uri for content provider.
     *
     * @return uri
     */
    public Uri getUri() {
        return uri;
    }

    /**
     * Set uri to convent provider.
     *
     * @param uri uri
     * @return key
     */
    public SqlKey setUri(Uri uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Get projection of columns.
     *
     * @return projection
     */
    public String[] getProjection() {
        return projection;
    }

    /**
     * Set projection of columns.
     *
     * @param projection projection
     * @return key
     */
    public SqlKey setProjection(String[] projection) {
        this.projection = projection;
        return this;
    }

    /**
     * Get where statement of query.
     *
     * @return selection
     */
    public String getSelection() {
        return selection;
    }

    /**
     * Set where statement of query.
     *
     * @param selection selection
     * @return key
     */
    public SqlKey setSelection(String selection) {
        this.selection = selection;
        return this;
    }

    /**
     * Get args of query.
     *
     * @return args
     */
    public String[] getSelectionArgs() {
        return selectionArgs;
    }

    /**
     * Set selection args for query.
     *
     * @param selectionArgs args
     * @return key
     */
    public SqlKey setSelectionArgs(String[] selectionArgs) {
        this.selectionArgs = selectionArgs;
        return this;
    }

    /**
     * Get sort order.
     *
     * @return sort order
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Set sort order.
     *
     * @param sortOrder sort order
     * @return key
     */
    public SqlKey setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
        return this;
    }

    /**
     * Get column that will be used by {@link SqliteRepository} for checking creation date.
     *
     * @return creation date column
     */
    public String getColumnCreationDate() {
        return columnCreationDate;
    }

    /**
     * Set column that will be used by {@link SqliteRepository} for checking creation date.
     *
     * @param columnCreationDate creation date column
     * @return key
     */
    public SqlKey setColumnCreationDate(String columnCreationDate) {
        this.columnCreationDate = columnCreationDate;
        return this;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        hashCode += objectHashCode(uri) * 31;
        hashCode += objectHashCode(projection) * 31;
        hashCode += objectHashCode(selection) * 31;
        //selection args not active
        hashCode += objectHashCode(sortOrder) * 31;
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

    @Override
    public String toString() {
        return uri == null ? super.toString() : uri.toString();
    }

}
