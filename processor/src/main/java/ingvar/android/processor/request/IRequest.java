package ingvar.android.processor.request;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IRequest<K, R> {

    K getRequestKey();

    Class<R> getResultClass();

    void cancel();

    boolean isCancelled();

    void setStatus(RequestStatus status);

    RequestStatus getStatus();

    void setMergeable(boolean mergeable);

    boolean isMergeable();

}
