package edu.upenn.cis.cis455.crawler.handlers;

import org.apache.commons.codec.digest.DigestUtils;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import static spark.Spark.halt;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class RegistrationHandler implements Route {
    StorageInterface db;
    
    public RegistrationHandler(StorageInterface db) {
        this.db = db;    
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        if (req.queryParams().contains("username") && req.queryParams().contains("password") &&
            req.queryParams().contains("firstName") && req.queryParams().contains("lastName")) {
            String sha256Password = DigestUtils.sha256Hex(req.queryParams("password"));
            if (db.usernameTaken(req.queryParams("username"))) {
                return "User already exists";
            } else {
                System.err.println("Adding " + req.queryParams("username") + "/" +
                    req.queryParams("password"));
                db.addUser(req.queryParams("username"), sha256Password, 
                           req.queryParams("firstName"), req.queryParams("lastName"));
                resp.redirect("/login-form");
            }
            
            
        } else
            halt(400, "Invalid form");
        
        return "";
    }
}
