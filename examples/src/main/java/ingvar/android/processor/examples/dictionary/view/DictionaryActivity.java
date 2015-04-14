package ingvar.android.processor.examples.dictionary.view;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionaries;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.examples.dictionary.task.CreateWordTask;
import ingvar.android.processor.examples.dictionary.task.DeleteDictionaryTask;
import ingvar.android.processor.examples.dictionary.task.RequestDictionariesTask;
import ingvar.android.processor.examples.dictionary.widget.DividerItemDecoration;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.observation.ActivityObserver;
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
        Word word = new Word("");
        word.setDictionary((Dictionary) dictionaries.getSelectedItem());
        getProcessor().execute(new CreateWordTask(word));
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

    private class RequestDictionariesObserver extends ActivityObserver<DictionaryActivity, Dictionaries> {

        public RequestDictionariesObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(Dictionaries result) {
            getActivity().dictionariesAdapter.swap(result);
        }

        @Override
        public void cancelled() {
            Toast.makeText(getActivity(), R.string.message_request_was_cancelled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void failed(Exception exception) {
            Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    private class DeleteDictionaryObserver extends ActivityObserver<DictionaryActivity, String> {

        public DeleteDictionaryObserver(DictionaryActivity activity) {
            super(activity);
        }

        @Override
        public void completed(String result) {
            Toast.makeText(getActivity(),
                getString(R.string.message_dictionary_deleted, result),
                Toast.LENGTH_LONG)
            .show();
            //update spinner
            getActivity().getProcessor()
                    .execute(new RequestDictionariesTask(DictionaryContract.Dictionaries.CONTENT_URI),
                            getActivity().requestDictionariesObserver);
        }

    }

}
