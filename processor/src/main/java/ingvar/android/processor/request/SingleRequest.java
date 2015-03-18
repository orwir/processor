package ingvar.android.processor.request;

/**
 * Created by Igor Zubenko on 2015.03.18.
 */
public interface SingleRequest<K, R> extends IRequest<K, R> {

    R loadFromExternalSource();
    Object getSourceType();

}
