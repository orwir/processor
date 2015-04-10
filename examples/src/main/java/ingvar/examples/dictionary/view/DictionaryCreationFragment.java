package ingvar.examples.dictionary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.request.SingleRequest;
import ingvar.android.processor.sqlite.source.SqliteSource;
import ingvar.examples.R;
import ingvar.examples.dictionary.pojo.Dictionary;
import ingvar.examples.dictionary.storage.DictionaryContract;
import roboguice.fragment.provided.RoboDialogFragment;
import roboguice.inject.InjectView;

/**
 * Created by Igor Zubenko on 2015.04.10.
 */
public class DictionaryCreationFragment extends RoboDialogFragment {

    @InjectView(R.id.dialog_input)
    private EditText dictionaryName;

    public static interface Listener {
        void dictionaryCreated(String name);
    }

    private Listener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (Listener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setView(View.inflate(getActivity(), R.layout.fragment_create_dictionary, null))
                .setTitle(R.string.label_dictionary_creation)
                .setPositiveButton(R.string.label_create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!dictionaryName.getText().toString().isEmpty()) {
                            
                        }
                    }
                })
                .setNegativeButton(R.string.label_cancel, null)
                .create();
    }

    private class CreationRequest extends SingleRequest<Uri, Dictionary, SqliteSource> {

        public CreationRequest(Uri key) {
            super(key, Dictionary.class, SqliteSource.class, Time.ALWAYS_RETURNED);
        }

        @Override
        public Dictionary loadFromExternalSource(IObserverManager observerManager, SqliteSource source) {
            Dictionary newDictionary = new Dictionary(dictionaryName.getText().toString());
            ContentValues values = source.getConverter(Dictionary.class).convert(newDictionary);
            source.getContentResolver().insert(DictionaryContract.Dictionary.CONTENT_URI, values);
            return null;
        }
    }

}
