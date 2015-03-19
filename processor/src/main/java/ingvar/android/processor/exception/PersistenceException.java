package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.03.19.
 */
public class PersistenceException extends ProcessorException {

    public PersistenceException() {
    }

    public PersistenceException(String detailMessage) {
        super(detailMessage);
    }

    public PersistenceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PersistenceException(Throwable throwable) {
        super(throwable);
    }

}
