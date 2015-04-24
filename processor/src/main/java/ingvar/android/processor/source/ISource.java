package ingvar.android.processor.source;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface ISource {

    /**
     * Check available source now or not.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();

}
