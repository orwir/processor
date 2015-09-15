package ingvar.android.processor.sqlite.persistence;

import android.provider.BaseColumns;

/**
 * Don't like to use interfaces as constants containers but it is Google-style.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.04.07.
 */
public interface ExtendedColumns extends BaseColumns {

    /**
     * Default column for creation time of object.
     * Used for {@link SqliteRepository#getCreationTime(Object)}.
     */
    String _CREATION_DATE = "_creation_date";

}
