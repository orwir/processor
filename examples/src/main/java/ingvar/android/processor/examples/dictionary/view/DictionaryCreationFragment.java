package ingvar.android.processor.examples.dictionary.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;
import ingvar.android.processor.examples.dictionary.task.CreateDictionaryTask;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.observation.ActivityObserver;
import ingvar.android.processor.service.Processor;
import roboguice.fragment.provided.RoboDialogFragment;

/**
 * Created by Igor Zubenko on 2015.04.10.
 */
public class DictionaryCreationFragment extends RoboDialogFragment {

    private EditText dictionaryName;
    private Processor processor;

    public interface Listener {
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
                            processor.planExecute(new CreateDictionaryTask(dictionaryName.getText().toString()),
                                    new CreationObserver(getActivity()));
                        }
                    }
                })
                .setNegativeButton(R.string.label_cancel, null)
                .create();
    }

    private class CreationObserver extends ActivityObserver<Activity, Dictionary> {

        public CreationObserver(Activity activity) {
            super(activity);
        }

        @Override
        public void completed(Dictionary result) {
            if(listener != null) {
                listener.dictionaryCreated(result.getName());
            }
        }

    }

}
