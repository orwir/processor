package ingvar.android.processor.examples.dictionary.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Meaning;
import ingvar.android.processor.examples.dictionary.pojo.Word;
import ingvar.android.processor.examples.dictionary.task.CreateMeaningTask;
import ingvar.android.processor.examples.dictionary.task.CreateWordTask;
import ingvar.android.processor.examples.dictionary.task.DeleteWordTask;
import ingvar.android.processor.examples.dictionary.task.RequestMeaningsTask;
import ingvar.android.processor.observation.ContextObserver;
import ingvar.android.processor.service.Processor;
import ingvar.android.processor.task.ITask;

/**
 * Created by Igor Zubenko on 2015.04.14.
 */
public class WordsAdapter extends RecyclerView.Adapter<WordsAdapter.Holder> {

    private WeakReference<Context> contextRef;
    private WeakReference<Processor> processorRef;
    private List<Word> words;

    public WordsAdapter(Context context, Processor processor) {
        this.contextRef = new WeakReference<>(context);
        this.processorRef = new WeakReference<>(processor);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_word, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Word word = words.get(position);

        holder.name.setText(word.getValue());
        if(holder.meaningsTask != null) {
            holder.meaningsTask.cancel();
        }

        if(word.getMeanings() == null || word.getMeanings().isEmpty()) {
            holder.meaningsTask = new RequestMeaningsTask(word, true);
            getProcessor().execute(holder.meaningsTask, new RequestMeaningsObserver(getContext(), holder));
        } else {
            holder.setMeanings(word.getMeanings());
        }
    }

    @Override
    public int getItemCount() {
        return words == null ? 0 : words.size();
    }

    public Word getItem(int position) {
        return words.get(position);
    }

    public void swap(List<Word> words) {
        this.words = words;
        notifyDataSetChanged();
    }

    public void removeByName(String word) {
        for(Word w : words) {
            if(w.getValue().equals(word)) {
                words.remove(w);
                break;
            }
        }
        notifyDataSetChanged();
    }

    protected Context getContext() {
        Context context = contextRef.get();
        if(context == null) {
            throw new IllegalStateException("Context is stale!");
        }
        return context;
    }

    protected Processor getProcessor() {
        Processor processor = processorRef.get();
        if(processor == null) {
            throw new IllegalStateException("Processor is stale!");
        }
        return processor;
    }

    class Holder extends RecyclerView.ViewHolder {

        private EditText name;
        private TextView meanings;
        private EditText createMeaning;
        private ImageButton remove;
        private ITask meaningsTask;

        public Holder(View itemView) {
            super(itemView);
            name = (EditText) itemView.findViewById(R.id.word_name);
            remove = (ImageButton) itemView.findViewById(R.id.word_remove);
            meanings = (TextView) itemView.findViewById(R.id.word_meanings);
            createMeaning = (EditText) itemView.findViewById(R.id.word_create_meaning);

            setMeanings(null);

            name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        Word word = getItem(getAdapterPosition());
                        word.setValue(name.getText().toString());
                        getProcessor().execute(new CreateWordTask(word), new UpdateWordObserver(getContext()));
                    }
                    return false;
                }
            });

            name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus) {
                        name.setTag(name.getText().toString());
                    }
                    else if(!name.getText().toString().equals(name.getTag())) {
                        Word word = getItem(getAdapterPosition());
                        word.setValue(name.getText().toString());
                        getProcessor().execute(new CreateWordTask(word), new UpdateWordObserver(getContext()));
                    }
                }
            });

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Word word = getItem(getAdapterPosition());
                    getProcessor().execute(new DeleteWordTask(word), new DeleteWordObserver(getContext()));
                }
            });

            createMeaning.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId == EditorInfo.IME_ACTION_DONE) {
                        String value = createMeaning.getText().toString();
                        if(value.isEmpty()) {
                            Toast.makeText(getContext(), R.string.message_meaning_empty, Toast.LENGTH_LONG).show();
                        } else {
                            Word word = getItem(getAdapterPosition());
                            Meaning meaning = new Meaning(word, value);
                            getProcessor().execute(new CreateMeaningTask(meaning), new CreateMeaningObserver(getContext(), word, Holder.this));
                            createMeaning.setText("");
                        }
                    }
                    return false;
                }
            });
        }

        public void setMeanings(List<Meaning> list) {
            if(list == null || list.isEmpty()) {
                meanings.setVisibility(View.GONE);
            } else {
                meanings.setVisibility(View.VISIBLE);
                StringBuilder sb = new StringBuilder();
                for(Meaning m : list) {
                    if(sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(m.getValue());
                }
                meanings.setText(sb.toString());
            }
        }

    }

    private class UpdateWordObserver extends ContextObserver<Context, Word> {

        public UpdateWordObserver(Context context) {
            super(context);
        }

        @Override
        public void completed(Word result) {
            int position = -1;
            for(Word word : words) {
                position++;
                if(word.getId().equals(result.getId())) {
                    word.setValue(result.getValue());
                    break;
                }
            }
            notifyItemChanged(position);
        }

        @Override
        public void failed(Exception exception) {
            Toast.makeText(getContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class DeleteWordObserver extends ContextObserver<Context, String> {


        public DeleteWordObserver(Context context) {
            super(context);
        }

        @Override
        public void completed(String result) {
            Toast.makeText(getContext(), getContext().getString(R.string.message_word_deleted, result), Toast.LENGTH_LONG).show();
            removeByName(result);
        }

    }

    private class CreateMeaningObserver extends ContextObserver<Context, Meaning> {

        private Word word;
        private Holder holder;

        public CreateMeaningObserver(Context context, Word word, Holder holder) {
            super(context);
            this.word = word;
            this.holder = holder;
        }

        @Override
        public void completed(Meaning result) {
            holder.meaningsTask = new RequestMeaningsTask(word, true);
            getProcessor().execute(
                    holder.meaningsTask,
                    new RequestMeaningsObserver(getContext(), holder));
        }
    }

    private class RequestMeaningsObserver extends ContextObserver<Context, List<Meaning>> {

        private Holder holder;

        public RequestMeaningsObserver(Context context, Holder holder) {
            super(context);
            this.holder = holder;
        }

        @Override
        public void completed(List<Meaning> result) {
            holder.setMeanings(result);
        }

    }

}
