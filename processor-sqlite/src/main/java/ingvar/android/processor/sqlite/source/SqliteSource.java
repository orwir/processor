package ingvar.android.processor.sqlite.source;

import android.content.ContentResolver;
import android.content.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.conversion.ConverterFactory;
import ingvar.android.processor.source.ContextSource;

/**
 * Base implementation of sqlite source.
 *
 * <br/><br/>Created by Igor Zubenko on 2015.04.07.
 */
public class SqliteSource extends ContextSource {

    private Map<Class, Converter> converters;

    public SqliteSource(Context context) {
        super(context);
        this.converters = new ConcurrentHashMap<>();
    }

    /**
     * Add object converter to source.
     *
     * @param dataClass data class.
     * @param converter
     */
    public void addConverter(Class dataClass, Converter converter) {
        converters.put(dataClass, converter);
    }

    public void removeConverter(Class dataClass) {
        converters.remove(dataClass);
    }

    public <T> Converter<T> getConverter(Class dataClass) {
        Converter converter = converters.get(dataClass);
        if(converter == null) {
            synchronized (dataClass) {
                converter = converters.get(dataClass);
                if(converter == null) {
                    converter = ConverterFactory.create(dataClass);
                    converters.put(dataClass, converter);
                }
            }
        }
        return converter;
    }

    public ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

}
