package ingvar.examples.dictionary.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import ingvar.android.literepo.conversion.Converter;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.request.SingleRequest;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.examples.R;
import ingvar.examples.dictionary.pojo.Dictionaries;
import ingvar.examples.dictionary.pojo.Dictionary;
import ingvar.examples.dictionary.storage.DictionaryContract;
import ingvar.examples.view.AbstractActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
@ContentView(R.layout.activity_dictionary)
public class DictionaryActivity extends AbstractActivity implements DictionaryCreationFragment.Listener {

    @InjectView(R.id.dictionaries)
    private Spinner dictionaries;
    private DictionaryAdapter dictionariesAdapter;
    private DictionaryObserver dictionariesObserver;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, DictionaryActivity.class));
    }

    public void createDictionary(View view) {
        DialogFragment dialog = new DictionaryCreationFragment();
        dialog.show(getFragmentManager(), "create_dictionary");
    }

    public void removeDictionary(View view) {
        //TODO: show alert
        //TODO: remove
        Toast.makeText(this, "not implemented yet", Toast.LENGTH_LONG).show();
    }

    @Override
    public void dictionaryCreated(String name) {
        //reload dictionaries
        processor.execute(new DictionaryRequest(DictionaryContract.Dictionary.CONTENT_URI), dictionariesObserver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dictionariesAdapter = new DictionaryAdapter(this);
        dictionaries.setAdapter(dictionariesAdapter);
        dictionariesObserver = new DictionaryObserver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dictionaries.getCount() == 0) {
            processor.planExecute(new DictionaryRequest(DictionaryContract.Dictionary.CONTENT_URI), dictionariesObserver);
        }
    }

    private class DictionaryObserver extends AbstractObserver<Dictionaries> {

        @Override
        public String getGroup() {
            return DictionaryActivity.class.getName();
        }

        @Override
        public void completed(Dictionaries result) {
            dictionariesAdapter.swap(result);
        }

        @Override
        public void cancelled() {
            Toast.makeText(DictionaryActivity.this, R.string.message_request_was_cancelled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void failed(Exception exception) {
            Toast.makeText(DictionaryActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class DictionaryRequest extends SingleRequest<Uri, Dictionaries, SqliteSource> {

        public DictionaryRequest(Uri key) {
            super(key, Dictionaries.class, SqliteSource.class, Time.ALWAYS_RETURNED);
        }

        @Override
        public Dictionaries loadFromExternalSource(IObserverManager observerManager, SqliteSource source) {
            Dictionaries dictionaries = new Dictionaries();

            Converter<Dictionary> converter = source.getConverter(Dictionary.class);
            Cursor cursor = source.getContentResolver().query(DictionaryContract.Dictionary.CONTENT_URI,
                    DictionaryContract.Dictionary.PROJECTION, null, null, null);

            while(cursor.moveToNext()) {
                dictionaries.add(converter.convert(cursor));
            }
            cursor.close();

            return dictionaries;
        }

    }

}
