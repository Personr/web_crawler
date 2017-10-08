package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;


public class HomeScreen implements Route {
    public HomeScreen() {
        
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        StringBuilder ret = new StringBuilder();
        
        ret.append("<html><head><title>Welcome to CIS 455/555 HW2</title></head>");
        ret.append("<body><h1>Welcome to CIS 455/555 HW2</h1>");
        ret.append("Welcome, " + req.attribute("user"));
        
        ret.append("<ul>");
        ret.append("<li><a href='/login-form.html'>Log in as a different user</a>");
        ret.append("<li><a href='/register.html'>Register a user</a>");
        ret.append("<li><a href='/logout'>Log out</a>");
        ret.append("</ul>");
        
        ret.append("</body></html>");
        
        resp.type("text/html");
        return ret.toString();
    }
}
