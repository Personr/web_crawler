package edu.upenn.cis.cis455.xpathengine;

import java.util.LinkedList;
import java.util.List;

public class XPathNode {
    
    protected String value; // value of the node (such as "html")
    protected String text; // text value for a node
    protected List<String> textTest = new LinkedList<>(); // text tests for an expression
    protected List<String> contains = new LinkedList<>(); // contains tests for an expression
    protected boolean expression; // True if it is part of an expression, false if it's a document node
    
    public XPathNode(String value, boolean expression) {
        this.value = value;
        this.expression = expression;
    }
    
    public String getValue() {
        return value;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void addContains(String contains) {
        this.contains.add(contains);
    }
    
    public void addTextTest(String textTest) {
        this.textTest.add(textTest);
    }
    
    /**
     * Returns true if the two nodes match
     * Ie, they have the same value and the text matches the different constraints of the expression
     */
    public boolean testMatch(XPathNode node) {
        if (expression) {
            throw new IllegalStateException("Method must be called on a node of document");
        }
        if (!node.expression) {
            throw new IllegalArgumentException("Method argument must be an expression");
        }
        
        if (!value.equals(node.value)) {
            return false;
        } else {
            if (node.textTest.isEmpty() && node.contains.isEmpty()) {
                return true;
            }
        }

        if (text != null) {
            for (String contains : node.contains) {
                if (text.contains(contains)) {
                    return true;
                }
            }
            for (String textTest : node.textTest) {
                if (text.equals(textTest)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    @Override
    public String toString() {
        String res = value;
        if (!expression) {
            return res + "[" + text + "]";
        }
        for (String textTest : this.textTest) {
            res += "[text() = \"" + textTest + "\"]";
        }
        for (String contains : this.contains) {
            res += "[contains(text(), \"" + contains + "\")]";;
        }
        return res;
    }
    
}
