package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class LexiconEntry implements Serializable {
    Integer wordId;
    String word;
    
    public Integer getWordId() {
        return wordId;
    }
    
    public String getWord() {
        return word;
    }
    
    @Override
    public int hashCode() {
        return wordId.hashCode();
    }
    
    public LexiconEntry(Integer wordId, String word) {
        this.wordId = wordId;
        this.word = word;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LexiconEntry))
            return false;
            
        LexiconEntry le = (LexiconEntry)o;
        
        return le.wordId.equals(wordId);
    }
    
}
