package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class ContextStaleException extends ProcessorException {

    public ContextStaleException() {
    }

    public ContextStaleException(String detailMessage) {
        super(detailMessage);
    }

    public ContextStaleException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ContextStaleException(Throwable throwable) {
        super(throwable);
    }
}
