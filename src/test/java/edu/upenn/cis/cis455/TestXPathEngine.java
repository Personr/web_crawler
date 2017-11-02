package edu.upenn.cis.cis455;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import edu.upenn.cis.cis455.xpathengine.XPathEngineImpl;

public class TestXPathEngine {
    
    XPathEngineImpl xpath;
    
    @Before
    public void setUp() {
        xpath = new XPathEngineImpl();
    }
    
    @Test
    public void testValidOneSimple() {
        String[] expressions = {"/a"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneSeveralChar() {
        String[] expressions = {"/aezezn"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneText() {
        String[] expressions = {"/a[text()=\"lkhvljkh\"]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneTextSpaces() {
        String[] expressions = {"/a[text()   = \"mjhm\"]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
}
