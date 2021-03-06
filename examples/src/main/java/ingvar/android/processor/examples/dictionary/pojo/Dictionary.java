package ingvar.android.processor.examples.dictionary.pojo;

import ingvar.android.literepo.conversion.annotation.Column;
import ingvar.android.literepo.conversion.annotation.Type;
import ingvar.android.processor.examples.dictionary.storage.DictionaryContract;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class Dictionary {

    @Column(value = DictionaryContract.Dictionaries.Col._ID, nullable = true, type = Type.INTEGER)
    private Long id;
    @Column(value = DictionaryContract.Dictionaries.Col.NAME, nullable = false, type = Type.TEXT)
    private String name;

    public Dictionary() {}

    public Dictionary(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
