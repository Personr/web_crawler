package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.info.URLInfo;

public interface CrawlMaster {
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(String site, int port, boolean isSecure);

    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site);
    
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url);
    
    /**
     * Returns true if this url hasn't been crawled during this crawl
     */
    public boolean isNotSeen(URLInfo url);
    
    /**
     * Notify that this url is being crawled not to crawl it again
     */
    public void setSeen(URLInfo url);
    
    /**
     * Returns true if the headInfo for this url
     * Returns null if no head request has been sent for this url
     */
    public HeadInfo getHeadInfo(URLInfo url);
    
    /**
     * Set the headInfo for a url
     */
    public void setHeadInfo(URLInfo url, HeadInfo headInfo);
    
    /**
     * Returns true is the size of the file is greater than the maximum size or non specified
     * or it has a wrong MIME type
     */
    public boolean checkHeadInfo(URLInfo url);
    
    /**
     * Returns true if the document has been modified since last time or if it hasn't been seen yet
     */
    public boolean shouldDownload(URLInfo url);
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content);
    
    /**
     * We've indexed another document
     */
    public void incCount();
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public boolean isDone();
    
    /**
     * Workers should notify when they are processing an URL
     */
    public void setWorking(boolean working);
    
    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public void notifyThreadExited();
}
