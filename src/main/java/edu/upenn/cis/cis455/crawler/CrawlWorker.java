package edu.upenn.cis.cis455.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CrawlWorker extends Thread {
    BlockingQueue<String> siteQueue;
    Map<String,List<String>> urlQueue;
    final CrawlMaster master;
    final StorageInterface db;
    
    public CrawlWorker(StorageInterface db, BlockingQueue<String> queue, Map<String,List<String>> urlQueue, CrawlMaster master) {
        setDaemon(true);
        this.db = db;
        this.siteQueue = queue;
        this.urlQueue = urlQueue;
        this.master = master;
    }
    
    public void run() {
        do {
            try {
                String url = siteQueue.take();
                
                if (url != null) {
                    master.setWorking(true);
                    crawl(url, urlQueue, siteQueue);
                    master.setWorking(false);
                }
            
            } catch (InterruptedException ie) {
                
            }

        } while (!master.isDone());
        //System.out.println("ouioui");
        master.notifyThreadExited();
    }
    
    
    public void crawl(String site, Map<String,List<String>> urlQueue, BlockingQueue<String> siteQueue) {
        URLInfo info = null;
        boolean parseUrl = false;
        boolean parseFromDb = false;
        do {
            List<String> urlsFromSite = urlQueue.get(site);
            parseUrl = false;
            parseFromDb = false;
            
            if (urlsFromSite != null && !urlsFromSite.isEmpty()) {
                info = new URLInfo(urlsFromSite.remove(0));
                System.out.println("Unqueued " + info.toString());
                    
                if (master.isOKtoCrawl(site, info.getPortNo(), info.isSecure())) {
                    // If we need to defer the crawl, put the URL back in its list
                    // and move the site to the back of the crawl queue
                    if (master.deferCrawl(site)) {
                        urlsFromSite.add(0, info.toString());
                        siteQueue.add(site);
                    } else if (master.isOKtoParse(info)) {
                        if (master.isNotSeen(info)) {
                            HeadInfo headInfo = master.getHeadInfo(info);
                            if (headInfo == null) {
                                System.out.println("Setting head info " + info.toString());
                                String lastModified = db.getDocumentLastModified(info.toString());
                                if (info.isSecure()) {
                                    headInfo = HttpClient.getHeadInfoSecured(info, lastModified);
                                } else {
                                    HttpClient connection = new HttpClient(info);
                                    headInfo = connection.getHeadInfo(lastModified);
                                }   
                                master.setHeadInfo(info, headInfo);
                                urlsFromSite.add(0, info.toString());
                                siteQueue.add(site);
                            } else {
                                if (master.shouldDownload(info)) {
                                    if (master.checkHeadInfo(info)) {
                                        parseUrl = true;
                                    }
                                } else {
                                    parseFromDb = true;
                                }


                                // Add back to the end of the queue
                                if (!urlsFromSite.isEmpty()) {
                                    siteQueue.add(site);
                                    // Nothing left from this site
                                } 
                                if (parseUrl || parseFromDb) {
                                    break;
                                }

                            }
                        }
                    }
                }
            } else {
                break;
            }
        }  while (!master.isDone());
        //System.out.println("oui");

        if (parseUrl || parseFromDb) {
            //System.out.println("ouiiiiiiiiiii");
            master.incCount();
            //crawled++;

            master.setSeen(info);
            // Parse
            if (parseUrl) {
                parseUrl(info);
            }
            if (parseFromDb) {
                parseFromDb(info);
            }
        }
    }
    
    public String parseText(URLInfo info, BufferedReader rd, boolean skipHeaders) {
        String line;
        StringBuilder response = new StringBuilder();
        try {
            if (skipHeaders) {
                line = rd.readLine();
                while (!line.equals("")) {
                    line = rd.readLine();
                }
            }
            while ((line = rd.readLine()) != null) {
                response.append(line);
                addLinks(info, line);
                response.append('\n');
            }
            rd.close();
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
    }
    
    public void parseFromDb(URLInfo info) {
        System.out.println("Parsing from db " + info.toString());
        String content = db.getDocument(info.toString());
        BufferedReader rd = new BufferedReader(new StringReader(content));
        parseText(info, rd, false);
    }
    
    public void parseUrl(URLInfo info) {
        System.out.println("Parsing url " + info.toString());

        try {
            URL url = new URL(info.toString());
            
            InputStream stream = null;
            
            if (info.isSecure()) {
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "cis455crawler");
                stream = connection.getInputStream();
            } else {
                HttpClient connection = new HttpClient(info);
                stream = connection.sendRequest("GET", null);
            }           
            BufferedReader rd = new BufferedReader(new InputStreamReader(stream));
            String response = parseText(info, rd, !info.isSecure());
            indexText(info.toString(), response);
        } catch (Exception mfe) {
            mfe.printStackTrace();
        } 

    }
    
    void addLinks(URLInfo info, String line) {
        String txt = line;//.toLowerCase();
        
//        System.out.println("Match " + txt);
        
        int href = txt.toLowerCase().indexOf("href");
        while (href >= 0 && txt.length() > 0 && href < txt.length()) {
            href += 4;
            
            boolean foundEquals = false;
            while (href < txt.length() &&
                (txt.charAt(href) == ' ' || txt.charAt(href) == '\t' || txt.charAt(href) == '=')
                ) {
                    foundEquals = (txt.charAt(href) == '=');
                href++;
            }

                
            if (foundEquals && 
            href >= 0 && href < txt.length()) {
                char quote = txt.charAt(href);

                // HREF
                if (quote == '\'' || quote == '\"') {
                    int end = txt.indexOf(quote, href+1);
                    if (end >= href) {
                        enqueueLink(info, txt.substring(href+1, end));
                    }
               }
               txt = txt.substring(href);
            } 
            href = txt.toLowerCase().indexOf("href");
        }
    }
    
    synchronized void addToQueue(String nextUrl) {
        //System.out.println("Next URL: " + nextUrl);
        
        URLInfo info = new URLInfo(nextUrl);
        
        if (!urlQueue.containsKey(info.getHostName()))
            urlQueue.put(info.getHostName(), new ArrayList<>());
            
        urlQueue.get(info.getHostName()).add(nextUrl);
        siteQueue.add(info.getHostName());
    }
    
    void enqueueLink(URLInfo info, String link) {
        //System.out.println("HREF: " + link);
        if (link.startsWith("/")) {
            String nextUrl = (info.isSecure() ? "https://" : "http://") +
                info.getHostName() + (info.getPortNo() == 80 ? "" : ":" + info.getPortNo()) +
                link;
                
            addToQueue(nextUrl);
        } else if (link.startsWith("http://") ||
            link.startsWith("https://")) {
                
            addToQueue(link);
        } else {
            String nextUrl = "";
            if (info.toString().endsWith("/"))
                nextUrl = info.toString() + link;
            else {
                nextUrl = info.toString().substring(0, info.toString().lastIndexOf('/')+1) + link;
            }
                
            addToQueue(nextUrl);
        }
    }
    
    void indexText(String url, String content) {
        if (db.getDocument(url) == null) {
            db.addDocument(url, content);
        } else {
            db.modifyDocument(url, content);
        }
    }
}
