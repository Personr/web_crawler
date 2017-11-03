package edu.upenn.cis.cis455;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.model.OccurrenceEvent.EventType;
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
    public void testValidSeveralNodes() {
        String[] expressions = {"/aezezn/fef/zf"};
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
        String[] expressions = {"/a[  text()   = \"mjhm\" ]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneContains() {
        String[] expressions = {"/a[contains(text(),\"mjhm\")]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneContainsSpaces() {
        String[] expressions = {"/a[ contains(   text() , \"mjhm\" )  ]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidOneTextConatains() {
        String[] expressions = {"/a[text()=\"lkhvljkh\"][contains(text(),\"mjhm\")]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidComplex() {
        String[] expressions = {"/gzzef/aezf[text()=\"lkhvljkh\"]/a[text()=\"lkhvljkh\"][contains(text(),\"mjhm\")]/efz/kj[contains(text(),\"mjhm\")][contains(text(),\"mjhm\")]"};
        xpath.setXPaths(expressions);
        
        assertTrue(xpath.isValid(0));
    }
    
    @Test
    public void testValidWrong() {
        String[] expressions = {"/a//ezf"};
        xpath.setXPaths(expressions);
        
        assertTrue(!xpath.isValid(0));
    }
    
    @Test
    public void testValidContainsWrong() {
        String[] expressions = {"/a[contains(text(,\"mjhm\")]"};
        xpath.setXPaths(expressions);
        
        assertTrue(!xpath.isValid(0));
    }
    
    @Test
    public void testValidTextWrong() {
        String[] expressions = {"/a[  text   = \"mjhm\" ]"};
        xpath.setXPaths(expressions);
        
        assertTrue(!xpath.isValid(0));
    }
    
    @Test
    public void testOccurenceSimple() {
        String[] expressions = {"/a", "/b"};
        xpath.setXPaths(expressions);
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        
        assertTrue(res[0]);
        assertTrue(!res[1]);
    }
    
    @Test
    public void testOccurenceSimpleDeep() {
        String[] expressions = {"/a/b/c", "/b"};
        xpath.setXPaths(expressions);
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "b"));
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "c"));
        
        assertTrue(res[0]);
        assertTrue(!res[1]);
    }
    
    @Test
    public void testOccurenceText() {
        String[] expressions = {"/a[text()=\"b\"]", "/b"};
        xpath.setXPaths(expressions);
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        assertTrue(!res[0]);
        assertTrue(!res[1]);
        
        res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.Text, "b"));
        assertTrue(res[0]);
        assertTrue(!res[1]);
    }
    
    @Test
    public void testOccurenceContains() {
        String[] expressions = {"/a[contains(text(,\"b\")]", "/b"};
        xpath.setXPaths(expressions);
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        assertTrue(!res[0]);
        assertTrue(!res[1]);
        
        res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.Text, "abc"));
        assertTrue(res[0]);
        assertTrue(!res[1]);
    }
    
    @Test
    public void testOccurenceClose() {
        String[] expressions = {"/a/b/c", "/a/d"};
        xpath.setXPaths(expressions);
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "b"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "c"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "c"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "b"));
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "d"));
        
        assertTrue(res[0]);
        assertTrue(res[1]);
    }
    
    @Test
    public void testOccurenceWrongClose() {
        String[] expressions = {"/a/b/c", "/a/d"};
        xpath.setXPaths(expressions);
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "b"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "c"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "c"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "b"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "p"));
        boolean[] res = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "d"));
        
        assertTrue(res[0]);
        assertTrue(res[1]);
    }
    
    @Test
    public void testOccurenceMixed() {
        String[] expressions = {"/a/b/c", "/a/d"};
        xpath.setXPaths(expressions);
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "a"));
        xpath.evaluateEvent(new OccurrenceEvent("doc2", EventType.ElementOpen, "a"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "b"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementOpen, "c"));
        xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "c"));
        boolean[] res1 = xpath.evaluateEvent(new OccurrenceEvent("doc1", EventType.ElementClose, "b"));
        boolean[] res2 = xpath.evaluateEvent(new OccurrenceEvent("doc2", EventType.ElementOpen, "d"));
        
        assertTrue(res1[0]);
        assertTrue(!res1[1]);
        assertTrue(!res2[0]);
        assertTrue(res2[1]);
    }
    
    
}
