package edu.upenn.cis.cis455.model;

import java.util.ArrayList;

import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

/**
 * An occurrence event is a tuple indicating the (document, event type, value)
 * as a document is being parsed.
 * 
 * For instance, we might get:
 *   ("doc1", ElementOpen, "html")
 *   ("doc1", ElementOpen, "body")
 *   ("doc1", ElementOpen, "title")
 *   ("doc1", Text, "My document")
 *   ("doc1", ElementClose, "title")
 *   ("doc1", ElementClose, "body")
 *   ("doc1", ElementClose, "body")
 */
public class OccurrenceEvent extends Tuple {
    public static enum EventType {ElementOpen, ElementClose, Text};

    public OccurrenceEvent(String docId, EventType type, String value) {
        super(new Fields("docId","type","value"), new ArrayList<Object>());
        
        getValues().add(docId);
        getValues().add(type);
        getValues().add(value);
        
        checkCardinality();
    }
    
    public String getDocId() {
        return (String)getValues().get(0);
    }
    
    public EventType getType() {
        return (EventType)getValues().get(1);
    }
    
    public String getValue() {
        return (String)getValues().get(2);
    }
}
