package ingvar.android.processor.exception;

/**
 * Root exception for whole framework
 *
 * <br/><br/>Created by Igor Zubenko on 2015.03.18.
 */
public class ProcessorException extends RuntimeException {

    public ProcessorException() {
    }

    public ProcessorException(String detailMessage) {
        super(detailMessage);
    }

    public ProcessorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ProcessorException(Throwable throwable) {
        super(throwable);
    }

}
