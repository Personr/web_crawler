package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;
import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.handlers.LogOut;
import edu.upenn.cis.cis455.crawler.handlers.LookupHandler;
import edu.upenn.cis.cis455.crawler.handlers.RegistrationHandler;
import edu.upenn.cis.cis455.crawler.handlers.HomeScreen;
import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;

public class WebInterface {
    public static void main(String args[]) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }
        
        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        port(8080);
        StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);
        
        LoginFilter testIfLoggedIn = new LoginFilter(database);
        
        if (args.length == 2) {
            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }

            
        before("/*", "POST", testIfLoggedIn);
        post("/register", new RegistrationHandler(database));
        HomeScreen home = new HomeScreen();
        get("/", home);
        get("/logout", new LogOut());
        get("/index.html", home);
        post("/login", new LoginHandler(database));
        
        get("/lookup", new LookupHandler(database));
        
        awaitInitialization();
    }
}
