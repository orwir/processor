package ingvar.android.processor.request;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public enum RequestStatus {

    PENDING,
    PROCESSING,
    LOADING_FROM_CACHE,
    LOADING_FROM_EXTERNAL,
    FAILED,
    COMPLETED;

}
