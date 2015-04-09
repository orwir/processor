package ingvar.examples.dictionary.pojo;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class Words extends ArrayList<Word> {

    public Words(int capacity) {
        super(capacity);
    }

    public Words() {
    }

    public Words(Collection<? extends Word> collection) {
        super(collection);
    }

}
