package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public class TaskCancelledException extends ProcessorException {

    public TaskCancelledException() {
    }

    public TaskCancelledException(String detailMessage) {
        super(detailMessage);
    }

    public TaskCancelledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TaskCancelledException(Throwable throwable) {
        super(throwable);
    }

}
