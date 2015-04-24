package ingvar.android.processor.examples.dictionary.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.examples.dictionary.task.CreateWordTask;
import ingvar.android.processor.examples.dictionary.task.DeleteDictionaryTask;
import ingvar.android.processor.examples.dictionary.task.RequestDictionariesTask;
import ingvar.android.processor.examples.dictionary.task.RequestWordsTask;
import ingvar.android.processor.examples.dictionary.widget.DividerItemDecoration;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.observation.ContextObserver;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
@ContentView(R.layout.activity_dictionary)
public class DictionaryActivity extends AbstractActivity implements DictionaryCreationFragment.Listener {

    @InjectView(R.id.dictionaries)
    private Spinner dictionaries;
    @InjectView(R.id.list_words)
    private RecyclerView words;
    private WordsAdapter wordsAdapter;

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
        getProcessor().execute(new DeleteDictionaryTask(dictionary.getName()), new DeleteDictionaryObserver(this));
    }

    public void createWord(View view) {
        Word word = new Word((Dictionary) dictionaries.getSelectedItem(), "");
        getProcessor().execute(new CreateWordTask(word), new CreateWordObserver(this));
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
        dictionaries.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Dictionary key = dictionariesAdapter.getItem(position);
                getProcessor().execute(new RequestWordsTask(key, false), new RequestWordsObserver(DictionaryActivity.this));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        words.setLayoutManager(new LinearLayoutManager(this));
        words.setHasFixedSize(true);
        words.addItemDecoration(new DividerItemDecoration(this));
        words.setAdapter(wordsAdapter = new WordsAdapter(this, getProcessor()));

        requestDictionariesObserver = new RequestDictionariesObserver(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(dictionaries.getCount() == 0) {
            getProcessor().planExecute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI), requestDictionariesObserver);
        }
    }

    private class RequestDictionariesObserver extends ContextObserver<DictionaryActivity, List<Dictionary>> {

        public RequestDictionariesObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(List<Dictionary> result) {
            getContext().dictionariesAdapter.swap(result);
        }

        @Override
        public void cancelled() {
            Toast.makeText(getContext(), R.string.message_request_was_cancelled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void failed(Exception exception) {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private class DeleteDictionaryObserver extends ContextObserver<DictionaryActivity, String> {

        public DeleteDictionaryObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(String result) {
            Toast.makeText(getContext(),
                getString(R.string.message_dictionary_deleted, result),
                Toast.LENGTH_LONG)
            .show();
            //update spinner
            getContext().getProcessor()
                    .execute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI),
                            getContext().requestDictionariesObserver);
        }

    }

    private class CreateWordObserver extends ContextObserver<DictionaryActivity, Word> {

        public CreateWordObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(Word result) {
            getContext().getProcessor().execute(
                    new RequestWordsTask((Dictionary) getContext().dictionaries.getSelectedItem(), true),
                    new RequestWordsObserver(getContext()));
        }

        @Override
        public void failed(Exception exception) {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private class RequestWordsObserver extends ContextObserver<DictionaryActivity, List<Word>> {

        public RequestWordsObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(List<Word> result) {
            wordsAdapter.swap(result);
        }

    }

}
