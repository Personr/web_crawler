package edu.upenn.cis.cis455;

import org.junit.Test;
import static org.junit.Assert.*;

import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;

public class TestCrawler {
    
    @Test
    public void testRobotsDisallow() {
        RobotsTxtInfo robot = new RobotsTxtInfo();
        robot.addUserAgent("cis455crawler");
        robot.addDisallowedLink("cis455crawler", "/test");
        
        assertTrue("Wrong output for disallow", !robot.isOk("/test"));
    }
    
    
    @Test
    public void testRobotsAllow() {
        RobotsTxtInfo robot = new RobotsTxtInfo();
        robot.addUserAgent("cis455crawler");
        robot.addUserAgent("*");
        robot.addDisallowedLink("*", "/test");
        robot.addAllowedLink("cis455crawler", "/test");
        
        assertTrue("Wrong output for allow", robot.isOk("/test"));
    }
    
}
