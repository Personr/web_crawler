package edu.upenn.cis.cis455.crawler;

public class HeadInfo {
    
    private String contentType;
    private int contentLength;
    private boolean modified;
    
    public HeadInfo(String contentType, int contentLength, boolean modified) {
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.modified = modified;
    }
    
    public int getContentLength() {
        return contentLength;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public boolean getModified() {
        return modified;
    }
    
    @Override
    public String toString() {
        return "Type : " + contentType + ", Length : " + contentLength + ", Modified : " + modified;
    }
    
}
