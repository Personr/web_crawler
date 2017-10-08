package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class KeywordEntry implements Serializable {
    Integer wordId;
    Integer docId;
    
    public Integer getWordId() {
        return wordId;
    }
    
    public Integer getDocId() {
        return docId;
    }
    
    public KeywordEntry(Integer wordId, Integer docId) {
        this.wordId = wordId;
        this.docId = docId;
    }
    
    @Override
    public int hashCode() {
        return wordId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof KeywordEntry) {
            KeywordEntry ke = (KeywordEntry)o;
            
            return ke.wordId.equals(wordId) && ke.docId.equals(docId);
        } else
            return false;
    }
}
