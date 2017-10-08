package edu.upenn.cis.cis455.crawler;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;


public class Crawler implements CrawlMaster {
    static final int NUM_WORKERS = 10;
    
    final String startUrl;
    final StorageInterface db;
    final int size;
    int count;
    int crawled = 0;
    int shutdown = 0;
    int busy = 0;
    
    Map<String, RobotsTxtInfo> robots = new HashMap<>();
    List<CrawlWorker> workers = new ArrayList<>();
    
    BlockingQueue<String> siteQueue = new LinkedBlockingQueue<>();
    Map<String,List<String>> urlQueue = new HashMap<>();
    // Last-crawled info for delays
    Map<String,Integer> lastCrawled = new HashMap<>();
    
    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        this.startUrl = startUrl;
        this.db = db;
        this.size = size;
        this.count = count;
    }
    
    public void start() {
        // Enqueue the first URL
        URLInfo info = new URLInfo(startUrl);
        urlQueue.put(info.getHostName(), new ArrayList<String>());
        urlQueue.get(info.getHostName()).add(startUrl);
        siteQueue.add(info.getHostName());
        
        // Launch 10 workers
        for (int i = 0; i < NUM_WORKERS; i++) {
            CrawlWorker worker = new CrawlWorker(db, siteQueue, urlQueue, this);
            worker.start();
        }
        System.out.println("Crawling started");
    }
    
    public boolean deferCrawl(String site) {
        return false;
    }
    
    @Override
    public boolean isOKtoCrawl(String site, int port, boolean isSecure) {
        if (!robots.containsKey(site)) {
            try {
                System.out.println("Site: " + site);
                URL url = new URL(isSecure ? "https://" : "http://" + site + ":" + port+ "/robots.txt");
                
                if (isSecure) {
                    HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
                    conn.disconnect();
                } else {
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.disconnect();
                }
                
                RobotsTxtInfo robot = new RobotsTxtInfo();
                
                // TOOD: fetch, parse
                
                robots.put(site, robot);
                
            } catch (Exception e) {
                e.printStackTrace();
            }

        } 
        return robots.get(site) == null || (robots.get(site).getDisallowedLinks("*") == null ||
            !robots.get(site).getDisallowedLinks("*").contains("/")) ||
            (robots.get(site).getDisallowedLinks("cis455crawler") == null ||
            !robots.get(site).getDisallowedLinks("cis455crawler").contains("/"));
    }

    @Override
    public boolean isOKtoParse(URLInfo url) {
        return true;
    }

    @Override
    public void incCount() {
        System.out.print(".");
        crawled++;
    }

    @Override
    public boolean isIndexable(String content) {
        return true;
    }
    
    @Override
    public boolean isDone() {
        return crawled >= count || (busy == 0 && siteQueue.isEmpty());
    }
    
    @Override
    public void notifyThreadExited() {
        shutdown++;
    }
    
    /**
     * Busy wait for shutdown
     */
    public void waitForThreadsToEnd() {
        while (shutdown < workers.size()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Threads are all shut down");
    }
    
    public void close() {
        db.close();
    }
    
    @Override
    public synchronized void setWorking(boolean working) {
        if (working)
            busy++;
        else
            busy--;
    }    



    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }
        
        System.out.println("Crawler starting");
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;
        
        StorageInterface db = StorageFactory.getDatabaseInstance(envPath);
        
        Crawler crawler = new Crawler(startUrl, db, size, count);
        
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.start();
        
        while (!crawler.isDone())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        crawler.waitForThreadsToEnd();
        crawler.close();
        System.out.println("Done crawling!");
    }

}
