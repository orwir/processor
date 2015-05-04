package ingvar.android.processor.task;

import java.util.Arrays;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public enum TaskStatus {

    PENDING,
    STARTED,
    LOADING_FROM_CACHE,
    PROCESSING,
    CANCELLED,
    FAILED,
    COMPLETED;

    public static boolean isFinalStatus(TaskStatus status) {
        return Arrays.asList(CANCELLED, FAILED, COMPLETED).contains(status);
    }

}
