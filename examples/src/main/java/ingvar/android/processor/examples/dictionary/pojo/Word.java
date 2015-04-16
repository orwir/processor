package ingvar.android.processor.examples.dictionary.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class Word {

    private Dictionary dictionary;
    private Long id;
    private String value;
    private List<Meaning> meanings;

    public Word() {}

    public Word(Dictionary dictionary, String value, Meaning... meanings) {
        this.dictionary = dictionary;
        this.value = value;
        this.meanings = Arrays.asList(meanings);
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<Meaning> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(Meaning meaning) {
        if(meanings == null) {
            meanings = new ArrayList<>();
        }
        meanings.add(meaning);
    }

    public void removeMeaning(String meaning) {
        if(meanings != null) {
            meanings.remove(meaning);
        }
    }

}
