package ingvar.android.processor.std.util;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.service.Processor;
import ingvar.android.processor.std.task.ImageRequest;
import ingvar.android.processor.util.CommonUtils;

/**
 * Created by Igor Zubenko on 2015.07.16.
 */
public class ImageLoader {

    public static void load(Processor processor, Uri uri, ImageView view, Drawable placeholder) {
        // TODO: 2015-08-03 use bitmap pool
        ImageRequest request = new ImageRequest(uri);
        ImageObserver observer = new ImageObserver(view, placeholder);
        view.setTag(request);
        if(placeholder != null) {
            view.setImageDrawable(placeholder);
        }
        processor.planExecute(request, observer);
    }

    public static void cancel(Processor processor, ImageView view) {
        // TODO: 2015-08-03 use bitmap pool
        if(processor.isBound()) {
            ImageRequest request = (ImageRequest) view.getTag();
            if (request != null) {
                processor.removeObservers(request);
            }
        }
    }

    private static class ImageObserver extends AbstractObserver<Bitmap> {

        private WeakReference<ImageView> mImageView;
        private Drawable mPlaceholder;

        public ImageObserver(ImageView view, Drawable placeholder) {
            mImageView = new WeakReference<>(view);
            mPlaceholder = placeholder;
        }

        @Override
        public String getGroup() {
            return ImageLoader.class.getName();
        }

        @Override
        public void completed(Bitmap bitmap) {
            ImageView view = CommonUtils.getReference(mImageView);
            if(view != null) {
                view.setTag(null);
                view.setImageBitmap(bitmap);
            }
        }

        @Override
        public void failed(Exception exception) {
            ImageView view = CommonUtils.getReference(mImageView);
            if(view != null) {
                view.setTag(null);
                view.setImageDrawable(mPlaceholder);
            }
        }
    }

    private ImageLoader() {}
}
