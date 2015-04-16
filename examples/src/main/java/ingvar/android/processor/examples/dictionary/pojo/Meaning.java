package ingvar.android.processor.examples.dictionary.pojo;

/**
 * Created by Igor Zubenko on 2015.04.16.
 */
public class Meaning {

    private Long id;
    private Word word;
    private String value;

    public Meaning() {}

    public Meaning(Word word, String value) {
        this.word = word;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
    
}
