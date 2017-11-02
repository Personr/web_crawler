package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.model.OccurrenceEvent;

public class XPathEngineImpl implements XPathEngine {
    
    private String[] expressions;

    @Override
    public void setXPaths(String[] expressions) {
        this.expressions = expressions;
        
    }

    @Override
    public boolean isValid(int i) {
        String textMatch = " *text\\(\\) *= *\".*\" *";
        String containsMatch = " *contains\\( *text\\(\\) *, *\".*\" *";
        String test = "((" + textMatch + ")|(" + containsMatch + "))";
        //test = textMatch;
        String nodename = "[a-z]+";
        String step = nodename + "(\\[" + test + "\\])*";
        String XPath = "(/" + step + ")+";
        
        return expressions[i].matches(XPath);
    }

    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {
        // TODO Auto-generated method stub
        return null;
    }
}
