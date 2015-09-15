package ingvar.android.processor.exception;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class SourceNotAvailable extends ProcessorException {

    public SourceNotAvailable() {
    }

    public SourceNotAvailable(String detailMessage) {
        super(detailMessage);
    }

    public SourceNotAvailable(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SourceNotAvailable(Throwable throwable) {
        super(throwable);
    }

}
