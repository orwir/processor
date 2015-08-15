package ingvar.android.processor.std.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Map;

import ingvar.android.processor.filesystem.util.FileUtils;
import ingvar.android.processor.network.source.NetworkSource;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.service.Processor;
import ingvar.android.processor.std.R;
import ingvar.android.processor.task.SingleTask;
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
            ImageRequest request = (ImageRequest) view.getTag(R.id.processor_irid);
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
        private IObserver<Bitmap> observer;

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

        public Request setObserver(IObserver<Bitmap> observer) {
            this.observer = observer;
            return this;
        }

        public SingleTask load(String uri, ImageView view) {
            return load(Uri.parse(uri), view);
        }

        public SingleTask load(Uri uri, ImageView view) {
            cancel(processor, view);

            view.setImageDrawable(placeholder);
            ImageRequest task = new ImageRequest(uri, options, width, height);
            Observer observer = new Observer(view, placeholder, this.observer);
            view.setTag(R.id.processor_irid, task);
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

    private static final class Observer extends AbstractObserver<Bitmap> {

        private WeakReference<ImageView> mImageView;
        private Drawable mPlaceholder;
        private IObserver<Bitmap> mObserver;

        private Observer(ImageView view, Drawable placeholder, IObserver<Bitmap> observer) {
            mImageView = new WeakReference<>(view);
            mPlaceholder = placeholder;
            mObserver = observer;
        }

        @Override
        public String getGroup() {
            return ImageLoader.class.getName();
        }

        @Override
        public void completed(Bitmap bitmap) {
            ImageView view = CommonUtils.getReference(mImageView);
            if(view != null) {
                view.setTag(R.id.processor_irid, null);
                view.setImageBitmap(bitmap);
            }
            if(mObserver != null) {
                mObserver.completed(bitmap);
            }
        }

        @Override
        public void failed(Exception exception) {
            ImageView view = CommonUtils.getReference(mImageView);
            if(view != null) {
                view.setTag(R.id.processor_irid, null);
                view.setImageDrawable(mPlaceholder);
            }
            if(mObserver != null) {
                mObserver.failed(exception);
            }
        }

        @Override
        public void cancelled() {
            super.cancelled();
            if(mObserver != null) {
                mObserver.cancelled();
            }
        }

        @Override
        public void progress(float progress, Map extra) {
            super.progress(progress, extra);
            if(mObserver != null) {
                mObserver.progress(progress, extra);
            }
        }
    }

    private ImageLoader() {}
}
