package ingvar.android.processor.examples.dictionary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import ingvar.android.literepo.builder.UriBuilder;
import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.service.Processor;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.android.processor.task.SingleTask;
import roboguice.fragment.provided.RoboDialogFragment;

/**
 * Created by Igor Zubenko on 2015.04.10.
 */
public class DictionaryCreationFragment extends RoboDialogFragment {

    private EditText dictionaryName;
    private Processor processor;

    public static interface Listener {
        void dictionaryCreated(String name);
    }

    private Listener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (Listener) activity;
        processor = ((AbstractActivity) activity).getProcessor();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.fragment_create_dictionary, null);
        dictionaryName = (EditText) view.findViewById(R.id.dialog_input);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.label_dictionary_creation)
                .setPositiveButton(R.string.label_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!dictionaryName.getText().toString().isEmpty()) {
                            processor.planExecute(new CreationTask(dictionaryName.getText().toString()), new CreationObserver());
                        }
                    }
                })
                .setNegativeButton(R.string.label_cancel, null)
                .create();
    }

    private class CreationObserver extends AbstractObserver<Dictionary> {

        @Override
        public String getGroup() {
            return getActivity().getClass().getName();
        }

        @Override
        public void completed(Dictionary result) {
            if(listener != null) {
                listener.dictionaryCreated(result.getName());
            }
        }

    }

    private class CreationTask extends SingleTask<String, Dictionary, SqliteSource> {

        public CreationTask(String key) {
            super(key, Dictionary.class, SqliteSource.class, Time.ALWAYS_RETURNED);
        }

        @Override
        public Dictionary process(IObserverManager observerManager, SqliteSource source) {
            Dictionary newDictionary = new Dictionary(getTaskKey());
            ContentValues values = source.getConverter(Dictionary.class).convert(newDictionary);
            source.getContentResolver().insert(DictionaryContract.Dictionaries.CONTENT_URI, values);

            Uri query = new UriBuilder()
                .authority(DictionaryContract.AUTHORITY)
                .table(DictionaryContract.Dictionaries.TABLE_NAME)
                .eq(DictionaryContract.Dictionaries.Col.NAME, getTaskKey())
            .build();
            Cursor cursor = source.getContentResolver().query(query, DictionaryContract.Dictionaries.PROJECTION, null, null, null);
            cursor.moveToFirst();
            Dictionary result = source.<Dictionary>getConverter(Dictionary.class).convert(cursor);
            cursor.close();

            return result;
        }
    }

}
