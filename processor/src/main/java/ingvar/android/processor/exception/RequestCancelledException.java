package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class RequestCancelledException extends ProcessorException {

    public RequestCancelledException() {
    }

    public RequestCancelledException(String detailMessage) {
        super(detailMessage);
    }

    public RequestCancelledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public RequestCancelledException(Throwable throwable) {
        super(throwable);
    }

}
