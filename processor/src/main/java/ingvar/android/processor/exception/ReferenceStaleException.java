package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class ReferenceStaleException extends ProcessorException {

    public ReferenceStaleException() {
    }

    public ReferenceStaleException(String detailMessage) {
        super(detailMessage);
    }

    public ReferenceStaleException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ReferenceStaleException(Throwable throwable) {
        super(throwable);
    }
}
