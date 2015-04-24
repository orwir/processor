package ingvar.android.processor.examples.gallery.task;

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ingvar.android.processor.exception.ProcessorException;
import ingvar.android.processor.filesystem.source.FilesystemSource;
import ingvar.android.processor.observation.IObserverManager;
import ingvar.android.processor.task.SingleTask;

/**
 * Created by Igor Zubenko on 2015.04.24.
 */
public class ImagesTask extends SingleTask<String, List<String>, FilesystemSource> {

    public ImagesTask(String assetDir) {
        super(assetDir, String.class, FilesystemSource.class);
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
