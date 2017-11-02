package edu.upenn.cis.cis455;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import edu.upenn.cis.cis455.storage.DBWrapper;
import edu.upenn.cis.cis455.storage.StorageInterface;

public class TestStorage {
    
    StorageInterface db;
    
    @Before
    public void setUp() {
        File dir = new File("./testDb");
        if (!dir.exists()) {
            dir.delete();
        }
        db = new DBWrapper("./testDb");
    }
    
    @Test
    public void testAddDocument() {
        db.addDocument("http://test1/", "test1");
        db.addDocument("http://test2/", "test2");
        
        assertEquals("Wrong content when adding a document", "test1", db.getDocument("http://test1/"));
        assertEquals("Wrong content when adding a document", "test2", db.getDocument("http://test2/"));
    }
    
    @Test
    public void testModifyDocument() throws InterruptedException {
        db.addDocument("http://test1/", "test1");
        db.modifyDocument("http://test1/", "test2");
        
        assertEquals("Wrong content when modifying a document", "test2", db.getDocument("http://test1/"));
    }
    
}
