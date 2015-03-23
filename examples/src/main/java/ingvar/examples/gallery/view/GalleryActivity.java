package ingvar.examples.gallery.view;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.filesystem.request.FilesystemRequest;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.util.CastUtils;
import ingvar.examples.R;
import ingvar.examples.gallery.widget.GalleryAdapter;
import ingvar.examples.view.AbstractActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectFragment;

/**
 * Created by Igor Zubenko on 2015.03.23.
 */
@ContentView(R.layout.activity_gallery)
public class GalleryActivity extends AbstractActivity {

    public static final String ASSETS_DIR = "gallery";
    private static final String TAG = GalleryActivity.class.getSimpleName();

    @InjectFragment(R.id.gallery_fragment)
    private ListFragment galleryFragment;
    private GalleryAdapter adapter;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, GalleryActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new GalleryAdapter(this, processor);
        galleryFragment.setListAdapter(adapter);
        //galleryFragment.getListView().setDivider(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter.isEmpty()) {
            Log.d(TAG, "load images list from assets dir: " + ASSETS_DIR);
            processor.planExecute(new ImagesRequest(ASSETS_DIR), new ImagesObserver());
        }
    }

    private class ImagesRequest extends FilesystemRequest<List<String>> {

        public ImagesRequest(String assetDir) {
            super(assetDir, CastUtils.<List<String>>cast(List.class), Time.ALWAYS_RETURNED);
        }

        @Override
        public List<String> loadFromExternalSource(IObserverManager observerManager, FilesystemSource source) {
            AssetManager assets = source.getAssetManager();
            try {
                return Arrays.asList(assets.list(getRequestKey()));
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
        }
    }

    private class ImagesObserver extends AbstractObserver<List<String>> {

        @Override
        public String getGroup() {
            return GalleryActivity.this.getClass().getSimpleName();
        }

        @Override
        public void completed(List<String> result) {
            Log.d(TAG, "images list loaded. Total: " + Integer.valueOf(result.size()));
            adapter.clear();
            adapter.addAll(result);
        }

        @Override
        public void failed(Exception exception) {
            adapter.clear();
            Toast.makeText(GalleryActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

}
