package edu.upenn.cis.cis455.crawler;

import static edu.upenn.cis.cis455.WebServiceController.*;

public class WebInterface {
    public static void main(String args[]) {
        get("/hello", (req, res) -> "Hello world");
        
        awaitInitialization();
    }
}
