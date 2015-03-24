package ingvar.android.processor.request;

/**
 *
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IRequest<K, R> extends Comparable<IRequest<K, R>> {

    /**
     * Return request key.
     *
     * @return unique identifier of request
     */
    K getRequestKey();

    /**
     * Return result class.
     *
     * @return class that represent the result
     */
    Class<R> getResultClass();

    /**
     * Notify worker what this request is cancelled.
     *
     * Trigger for method {@link ingvar.android.processor.observation.IObserver#cancelled()}
     */
    void cancel();

    /**
     * Check if request cancelled or not.
     *
     * @return true - if request cancelled, false otherwise
     */
    boolean isCancelled();

    void setStatus(RequestStatus status);

    RequestStatus getStatus();

    void setMergeable(boolean mergeable);

    boolean isMergeable();

}
