package ingvar.android.processor.std.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;

import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.network.source.NetworkSource;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.service.Processor;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.util.BitmapUtils;
import ingvar.android.processor.util.CommonUtils;
import ingvar.android.processor.util.PooledBitmapDecoder;

/**
 * Created by Igor Zubenko on 2015.07.16.
 */
public class ImageLoader {

    public static Request request(Processor processor) {
        return new Request(processor);
    }

    public static void cancel(Processor processor, ImageView view) {
        if(processor.isBound()) {
            ImageRequest request = (ImageRequest) view.getTag();
            if (request != null) {
                processor.removeObservers(request);
            }
        }
    }

    public static final class Request {

        private Processor processor;
        private Drawable placeholder;
        private BitmapFactory.Options options;
        private int width = -1;
        private int height = -1;

        private Request(Processor processor) {
            this.processor = processor;
        }

        public Request setPlaceholder(Drawable placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        public Request setOptions(BitmapFactory.Options options) {
            this.options = options;
            return this;
        }

        public Request setSize(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public SingleTask load(String uri, ImageView view) {
            return load(Uri.parse(uri), view);
        }

        public SingleTask load(Uri uri, ImageView view) {
            if(view.getDrawable() != null && !view.getDrawable().equals(placeholder)) {
                PooledBitmapDecoder.free(BitmapUtils.tryGetBitmapFromDrawable(view.getDrawable()));
            }
            cancel(processor, view);

            view.setImageDrawable(placeholder);
            ImageRequest task = new ImageRequest(uri, options, width, height);
            ImageObserver observer = new ImageObserver(view, placeholder);
            processor.planExecute(task, observer);
            return task;
        }

    }

    public static final class ImageRequest extends SingleTask<Uri, Bitmap, NetworkSource> {

        private BitmapFactory.Options options;
        private int width;
        private int height;

        private ImageRequest(Uri uri, BitmapFactory.Options options, int width, int height) {
            super(uri, Bitmap.class, NetworkSource.class, Time.ALWAYS_RETURNED);
            this.options = (options == null) ? new BitmapFactory.Options() : options;
            this.width = width;
            this.height = height;
        }

        @Override
        public Bitmap process(IObserverManager observerManager, NetworkSource source) {
            if(source.isAvailable()) {
                InputStream is = source.download(getTaskKey());
                Bitmap bitmap = PooledBitmapDecoder.decode(is, options, width, height);
                FileUtils.close(is);
                return bitmap;
            }
            throw new IllegalStateException("Network is not available!");
        }

    }

    public static final class ImageObserver extends AbstractObserver<Bitmap> {

        private WeakReference<ImageView> mImageView;
        private Drawable mPlaceholder;

        private ImageObserver(ImageView view, Drawable placeholder) {
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
