package ingvar.android.processor.examples.dictionary.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.dictionary.pojo.Dictionary;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class DictionaryAdapter extends BaseAdapter {

    private WeakReference<Context> contextRef;
    private List<Dictionary> dictionaries;
    private LayoutInflater inflater;

    public DictionaryAdapter(Context context) {
        contextRef = new WeakReference<>(context);
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void swap(List<Dictionary> dictionaries) {
        this.dictionaries = dictionaries;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return dictionaries == null ? 0 : dictionaries.size();
    }

    @Override
    public Dictionary getItem(int position) {
        return dictionaries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.spinner_item, parent, false);
        }
        Dictionary dictionary = getItem(position);
        ((TextView) convertView).setText(dictionary.getName());
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }

}
