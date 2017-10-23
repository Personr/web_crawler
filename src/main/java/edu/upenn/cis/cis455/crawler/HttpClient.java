package edu.upenn.cis.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis.cis455.crawler.info.URLInfo;

public class HttpClient {
    
    private Socket socket;
    private URLInfo info;
    
    public HttpClient(URLInfo info) {
        this.info = info;
        try {
            socket = new Socket(info.getHostName(), info.getPortNo());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public InputStream sendRequest(String requestType, String lastModified) {
        try {
            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, false);
            String request = requestType + " " + info.getFilePath() + " HTTP/1.1\r\n" +
                             "Host: " + info.getHostName() + "\r\n" + 
                             ((lastModified != null) ? ("If-Modified-Since: " + lastModified + "\r\n") : "") +
                             "User-Agent: cis455crawler\r\n\r\n";
            writer.print(request);
            //System.out.println(request.replace("\r", ""));
            writer.flush();
            return socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static String getHeader(String line, String name) {
        if (line.startsWith(name)) {
            line = line.replace(name + ": ", "");
            return line;
        }
        return null;
    }
    
    @SuppressWarnings("finally")
    private static HeadInfo parseHeadResponse(BufferedReader rd) {
        String contentType = "";
        int contentLength = -1;
        String line;
        try {
            line = rd.readLine();
            if (line.contains("304")) { //Not modified since last time
                return new HeadInfo("", -1, false);
            }
            while ((line = rd.readLine()) != null) {
                String contentTypeTemp = getHeader(line, "Content-Type");
                if (contentTypeTemp != null) {
                    contentType = contentTypeTemp;
                }
                String contentLengthTemp = getHeader(line, "Content-Length");
                if (contentLengthTemp != null) {
                    contentLength = Integer.valueOf(contentLengthTemp);
                }   
                if (!contentType.equals("") && contentLength != -1 || line.contains("keep-alive")) {
                    break;
                }
            }
            rd.close();
            
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return new HeadInfo(contentType, contentLength, true);
        }
    }
    
    public HeadInfo getHeadInfo(String lastModified) {
        BufferedReader rd = new BufferedReader(new InputStreamReader(sendRequest("HEAD", lastModified)));
        return parseHeadResponse(rd);
     
    }
    
    public static HeadInfo getHeadInfoSecured(URLInfo info, String lastModified) {
        URL url;
        try {
            url = new URL(info.toString());
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestProperty("User-Agent", "cis455crawler");
            if (lastModified != null) {
                connection.setRequestProperty("If-Modified-Since", lastModified);
            }
            connection.setRequestMethod("HEAD");
            InputStream stream = connection.getInputStream();
            return new HeadInfo(connection.getContentType(), connection.getContentLength(), connection.getResponseCode() != 304);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }   
        
    }
    
}
