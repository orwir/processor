package ingvar.android.processor.examples.gallery.view;

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

import ingvar.android.processor.examples.R;
import ingvar.android.processor.examples.gallery.widget.GalleryAdapter;
import ingvar.android.processor.examples.view.AbstractActivity;
import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.AbstractObserver;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.persistence.Time;
import ingvar.android.processor.task.SingleTask;
import ingvar.android.processor.util.CastUtils;
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

        adapter = new GalleryAdapter(this, getProcessor());
        galleryFragment.setListAdapter(adapter);
        galleryFragment.getListView().setDivider(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter.isEmpty()) {
            Log.d(TAG, "load images list from assets dir: " + ASSETS_DIR);
            getProcessor().planExecute(new ImagesTask(ASSETS_DIR), new ImagesObserver());
        }
    }

    private class ImagesTask extends SingleTask<String, List<String>, FilesystemSource> {

        public ImagesTask(String assetDir) {
            super(assetDir, CastUtils.<List<String>>cast(List.class), FilesystemSource.class, Time.ALWAYS_RETURNED);
        }

        @Override
        public List<String> process(IObserverManager observerManager, FilesystemSource source) {
            AssetManager assets = source.getAssetManager();
            try {
                return Arrays.asList(assets.list(getTaskKey()));
            } catch (IOException e) {
                throw new ProcessorException(e);
            }
        }
    }

    private class ImagesObserver extends AbstractObserver<List<String>> {

        @Override
        public String getGroup() {
            return GalleryActivity.this.getClass().getName();
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
