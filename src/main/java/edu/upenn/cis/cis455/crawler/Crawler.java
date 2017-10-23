package edu.upenn.cis.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    Set<String> crawledUrls = new HashSet<String>();
    Map<String, HeadInfo> urlHeads = new HashMap<>();
    
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
            workers.add(worker);
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
                String urlString = (isSecure ? "https://" : "http://") + site + ((port != 80) ? ":" + port : "") + "/robots.txt";
                URL url = new URL(urlString);
                InputStream stream = null;
                if (isSecure) {
                    HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
                    connection.setRequestProperty("User-Agent", "cis455crawler");
                    stream = connection.getInputStream();
                } else {
                    URLInfo info = new URLInfo(urlString);
                    HttpClient connection = new HttpClient(info);
                    stream = connection.sendRequest("GET", null);
                }
                
                RobotsTxtInfo robot = new RobotsTxtInfo();

                String line;
                BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
                try {
                    String userAgent = "";
                    while ((line = rd.readLine()) != null) {
                        if (line.startsWith("User-agent:")) {
                            userAgent = line.replace("User-agent: ", "");
                            robot.addUserAgent(userAgent);
                        } else if (userAgent.equals("*") || userAgent.equals("cis455crawler")) {
                            if (line.startsWith("Disallow:")) {
                                robot.addDisallowedLink(userAgent, line.replace("Disallow: ", ""));
                            } else if (line.startsWith("Allow:")) {
                                robot.addAllowedLink(userAgent, line.replace("Allow: ", ""));
                            } else if (line.startsWith("Crawl-delay:")) {
                                robot.addCrawlDelay(userAgent, Integer.valueOf(line.replace("Crawl-delay: ", "")));
                            } else if (line.startsWith("Sitemap:")) {
                                robot.addSitemapLink(line.replace("Sitemap: ", ""));
                            }
                        }
                    }
                    rd.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                robots.put(site, robot);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } 
        if (robots.get(site) == null) {
            return true;
        } else {
            return robots.get(site).isOk("/");
        }
    }

    @Override
    public boolean isOKtoParse(String site, URLInfo url) {
        if (robots.get(site) == null) {
            return true;
        } else {
            return robots.get(site).isOk(url.getFilePath());
        }
    }
    
    @Override
    public boolean isNotSeen(URLInfo url) {
        return !crawledUrls.contains(url.toString());
    }

    @Override
    public void setSeen(URLInfo url) {
        crawledUrls.add(url.toString());
        
    }
    
    @Override
    public HeadInfo getHeadInfo(URLInfo url) {
        return urlHeads.get(url.toString());
    }
    
    @Override
    public void setHeadInfo(URLInfo url, HeadInfo headInfo) {
        urlHeads.put(url.toString(), headInfo);
    }
    
    @Override
    public boolean checkHeadInfo(URLInfo url) {
        HeadInfo headInfo = urlHeads.get(url.toString());
        int contentLength = headInfo.getContentLength();
        String contentType = headInfo.getContentType();
        return contentLength != -1 && contentLength <= size * 1000000 && 
               (contentType.contains("text/html") || contentType.contains("text/xml") || contentType.contains("application/xml") || contentType.contains("+xml"));
    }
    
    @Override
    public boolean shouldDownload(URLInfo url) {
        HeadInfo headInfo = urlHeads.get(url.toString());
        return headInfo.getModified();
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
        synchronized(this) {
            shutdown++;
        }
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
                e.printStackTrace();
            }
            
        crawler.waitForThreadsToEnd();
        crawler.close();
        System.out.println("Done crawling!");
    }

}
