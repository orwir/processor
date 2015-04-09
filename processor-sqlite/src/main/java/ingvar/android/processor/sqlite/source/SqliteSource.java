package ingvar.android.processor.sqlite.source;

import android.content.ContentResolver;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.literepo.conversion.ConverterFactory;
import ingvar.android.processor.source.ISource;

/**
 * Created by Igor Zubenko on 2015.04.07.
 */
public class SqliteSource implements ISource {

    private WeakReference<Context> contextRef;
    private Map<Class, Converter> converters;

    public SqliteSource(Context context) {
        this.contextRef = new WeakReference<>(context);
        this.converters = new HashMap<>();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    public void addConverter(Class dataClass, Converter converter) {
        converters.put(dataClass, converter);
    }

    public void removeConverter(Class dataClass) {
        converters.remove(dataClass);
    }

    public Converter getConverter(Class dataClass) {
        Converter converter = converters.get(dataClass);
        if(converter == null) {
            converter = ConverterFactory.create(dataClass);
            converters.put(dataClass, converter);
        }
        return converter;
    }

    public Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

    public ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }

}
