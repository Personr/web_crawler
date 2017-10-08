package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class UrlEntry implements Serializable {
    Integer docId;
    String url;
    
    public Integer getDocId() {
        return docId;
    }
    
    public String getUrl() {
        return url;
    }
    
    public UrlEntry(Integer docId, String url) {
        this.docId = docId;
        this.url = url;
    }
    
    @Override
    public int hashCode() {
        return docId.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CorpusEntry))
            return false;
            
        return ((UrlEntry)o).docId.equals(docId);
    }
}
