package ingvar.android.processor.examples.dictionary.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.literepo.conversion.Converter;
import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionaries;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;
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
    private RequestDictionariesObserver requestDictionariesObserver;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, DictionaryActivity.class));
    }

    public void createDictionary(View view) {
        DialogFragment dialog = new DictionaryCreationFragment();
        dialog.show(getFragmentManager(), "create_dictionary");
    }

    public void removeDictionary(View view) {
        Dictionary dictionary = (Dictionary) dictionaries.getSelectedItem();
        getProcessor().execute(new DeleteDictionaryTask(dictionary.getName()), new DeleteDictionaryObserver());
    }

    @Override
    public void dictionaryCreated(String name) {
        //reload dictionaries
        getProcessor().execute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI), requestDictionariesObserver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dictionariesAdapter = new DictionaryAdapter(this);
        dictionaries.setAdapter(dictionariesAdapter);
        requestDictionariesObserver = new RequestDictionariesObserver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dictionaries.getCount() == 0) {
            getProcessor().planExecute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI), requestDictionariesObserver);
        }
    }

    private class RequestDictionariesObserver extends AbstractObserver<Dictionaries> {

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

    private class RequestDictionariesTask extends SingleTask<Uri, Dictionaries, SqliteSource> {

        public RequestDictionariesTask(Uri key) {
            super(key, Dictionaries.class, SqliteSource.class, Time.ALWAYS_EXPIRED);
        }

        @Override
        public Dictionaries process(IObserverManager observerManager, SqliteSource source) {
            Dictionaries dictionaries = new Dictionaries();

            Converter<Dictionary> converter = source.getConverter(Dictionary.class);
            Cursor cursor = source.getContentResolver().query(DictionaryContract.Dictionaries.CONTENT_URI,
                    DictionaryContract.Dictionaries.PROJECTION, null, null, null);

            while(cursor.moveToNext()) {
                dictionaries.add(converter.convert(cursor));
            }
            cursor.close();

            return dictionaries;
        }

    }

    private class DeleteDictionaryObserver extends AbstractObserver<String> {

        @Override
        public String getGroup() {
            return DictionaryActivity.class.getName();
        }

        @Override
        public void completed(String result) {
            Toast.makeText(DictionaryActivity.this,
                getString(R.string.message_dictionary_deleted, result),
                Toast.LENGTH_LONG)
            .show();
            getProcessor().execute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI), requestDictionariesObserver);
        }

    }

    private class DeleteDictionaryTask extends SingleTask<String, String, SqliteSource> {

        public DeleteDictionaryTask(String key) {
            super(key, String.class, SqliteSource.class, Time.ALWAYS_EXPIRED);
        }

        @Override
        public String process(IObserverManager observerManager, SqliteSource source) {
            Uri uri = new UriBuilder()
                .authority(DictionaryContract.AUTHORITY)
                .table(DictionaryContract.Dictionaries.TABLE_NAME)
                .eq(DictionaryContract.Dictionaries.Col.NAME, getTaskKey())
            .build();
            source.getContentResolver().delete(uri, null, null);
            return getTaskKey();
        }

    }

}
