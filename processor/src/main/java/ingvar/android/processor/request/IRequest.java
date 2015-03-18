package ingvar.android.processor.request;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface IRequest<K, R> {

    K getRequestKey();
    Class<R> getResultClass();
    long getExpirationTime();
    boolean isCancelled();
    void setStatus(RequestStatus status);
    RequestStatus getStatus();

}
