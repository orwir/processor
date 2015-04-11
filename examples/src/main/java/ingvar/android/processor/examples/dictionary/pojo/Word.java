package ingvar.android.processor.examples.dictionary.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Igor Zubenko on 2015.04.09.
 */
public class Word {

    private Long id;
    private String word;
    private List<String> meanings;

    public Word(String word, String... meanings) {
        this.word = word;
        this.meanings = Arrays.asList(meanings);
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<String> meanings) {
        this.meanings = meanings;
    }

    public void addMeaning(String meaning) {
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
