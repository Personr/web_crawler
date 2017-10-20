package edu.upenn.cis.cis455.crawler.handlers;

import org.apache.commons.codec.digest.DigestUtils;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class LoginHandler implements Route {
    StorageInterface db;
    
    public LoginHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String user = req.queryParams("username");
        String pass = req.queryParams("password");
        String sha256Pass = DigestUtils.sha256Hex(pass);
        
        System.err.println("Login request for " + user + " and " + pass);
        if (db.getSessionForUser(user, sha256Pass)) {
            System.err.println("Logged in!");
            Session session = req.session();
            
            session.attribute("user", user);
            session.attribute("password", sha256Pass);
            session.maxInactiveInterval(300);
            resp.redirect("/index.html");
        } else {
            System.err.println("Invalid credentials");
            resp.redirect("/login-form.html");
        }

            
        return "";
    }
}
