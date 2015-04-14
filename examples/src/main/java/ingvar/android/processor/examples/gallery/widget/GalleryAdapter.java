package ingvar.android.processor.examples.gallery.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.gallery.task.ImageTask;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.service.Processor;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
public class GalleryAdapter extends ArrayAdapter<String> {

    private Processor processor;

    public GalleryAdapter(Context context, Processor processor) {
        super(context, R.layout.layout_image);
        this.processor = processor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = View.inflate(getContext(), R.layout.layout_image, null);
            Holder holder = new Holder();
            convertView.setTag(holder);

            holder.name = (TextView) convertView.findViewById(R.id.image_name);
            holder.view = (ImageView) convertView.findViewById(R.id.image_view);
        }
        Holder holder = (Holder) convertView.getTag();

        synchronized (convertView) {
            holder.name.setText(getItem(position));
            holder.view.setImageResource(R.drawable.image_placeholder);
            if (holder.request != null) {
                holder.request.cancel();
            }
            holder.request = new ImageTask(getItem(position));
            processor.execute(holder.request, new ImageObserver(holder));
        }

        return convertView;
    }

    private class Holder {
        TextView name;
        ImageView view;

        ImageTask request;
    }

    private class ImageObserver extends AbstractObserver<Bitmap> {

        private Holder holder;

        public ImageObserver(Holder holder) {
            this.holder = holder;
        }

        @Override
        public String getGroup() {
            return getContext().getClass().getName();
        }

        @Override
        public void completed(Bitmap result) {
            holder.view.setImageBitmap(result);
        }

    }

}
