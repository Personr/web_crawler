package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Response;
import spark.Route;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class LookupHandler implements Route {
    final StorageInterface db;
    
    public LookupHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws Exception {
        resp.type("text/html");
        return db.getDocument(req.queryParams("url"));
    }
    
    
}
