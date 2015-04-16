package ingvar.android.processor.examples.dictionary.pojo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Igor Zubenko on 2015.04.16.
 */
public class Meanings extends ArrayList<Meaning> {
    public Meanings(int capacity) {
        super(capacity);
    }

    public Meanings() {
    }

    public Meanings(Collection<? extends Meaning> collection) {
        super(collection);
    }
}
