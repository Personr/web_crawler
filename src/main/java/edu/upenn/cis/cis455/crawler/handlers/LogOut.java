package edu.upenn.cis.cis455.crawler.handlers;

import spark.Session;

import spark.Request;
import spark.Response;
import spark.Route;

public class LogOut implements Route {

    @Override
    public String handle(Request req, Response resp) throws Exception {
        Session sess = req.session(false);
        sess.invalidate();
        resp.redirect("/login.html");
        return "";
    }
}
