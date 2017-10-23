package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class CorpusEntry implements Serializable {
    Integer docId;
    String content;
    
    public Integer getDocId() {
        return docId;
    }
    
    public String getContent() {
        return content;
    }
    
    public CorpusEntry(Integer docId, String content) {
        this.docId = docId;
        this.content = content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    @Override
    public int hashCode() {
        return docId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CorpusEntry))
            return false;
            
        return ((CorpusEntry)o).docId.equals(docId);
    }
}
