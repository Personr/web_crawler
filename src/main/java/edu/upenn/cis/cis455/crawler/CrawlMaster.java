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
