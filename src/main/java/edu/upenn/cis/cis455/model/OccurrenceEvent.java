package edu.upenn.cis.cis455.model;

import java.util.ArrayList;

import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;

public class OccurrenceEvent extends Tuple {
    public static enum EventType {ElementOpen, ElementClose, Text};

    public OccurrenceEvent(String docId, int type, String value) {
        super(new Fields("docId","type","value"), new ArrayList<Object>());
        
        getValues().add(docId);
        getValues().add(type);
        getValues().add(value);
    }
    
}
