package edu.upenn.cis.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.model.OccurrenceEvent.EventType;

public class XPathEngineImpl implements XPathEngine {

    private String[]                     expressions; // Raw expressions
    private List<List<XPathNode>>        expressionNodes; // Expressions parsed as lists of XPathNode to make it easier to compare
    private Map<String, boolean[]>       states = new HashMap<>(); // The states of the different documents wrt to the expressions
    private Map<String, List<XPathNode>> nodes  = new HashMap<>(); // For each document, a list of XPathNode representing the path from the root to the current node

    @Override
    public void setXPaths(String[] expressions) {
        this.expressions = expressions;
        expressionNodes = new ArrayList<>();

        // Parse the expressions to lists of XPathNode
        for (int i = 0; i < expressions.length; i++) {
            expressionNodes.add(new ArrayList<>());
            String[] nodes = expressions[i].split("/");
            for (String node : nodes) {
                if (!node.equals("")) {
                    String[] parts = node.split("\\[");
                    XPathNode xPathNode = new XPathNode(parts[0], true);
                    for (String part : parts) {
                        if (part.contains("contains")) {
                            xPathNode.addContains(part.split("\"")[1]);
                        } else if (part.contains("text")) {
                            xPathNode.addTextTest(part.split("\"")[1]);
                        }
                    }
                    expressionNodes.get(i).add(xPathNode);
                }
            }
        }

    }

    @Override
    public boolean isValid(int i) {
        if (expressions == null) {
            return false;
        }
        String textMatch = " *text\\(\\) *= *\".*\" *";
        String containsMatch = " *contains\\( *text\\(\\) *, *\".*\" *\\) *";
        String test = "((" + textMatch + ")|(" + containsMatch + "))";
        String nodename = "[a-z]+";
        String step = nodename + "(\\[" + test + "\\])*";
        String XPath = "(/" + step + ")+";

        return expressions[i].matches(XPath);
    }
    
    /**
     * Update the doc state given the current node
     */
    private void updateState(String docId) {
        List<XPathNode> nodeList = nodes.get(docId);
        boolean[] state = states.get(docId);
        
        // For each expression...
        for (int i = 0; i < expressionNodes.size(); i++) {
            List<XPathNode> expressionNodeList = expressionNodes.get(i);
            if (!state[i] && expressionNodeList.size() == nodeList.size()) {
                 boolean match = true;
                 
                 // ...compare every node
                 for (int j = 0; j < nodeList.size(); j ++) {
                     match = match && nodeList.get(j).testMatch(expressionNodeList.get(j));
                 }
                 state[i] = match;
            }
        }
        states.put(docId, state);
    }

    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {
        String docId = event.getDocId();
        EventType type = event.getType();
        String value = event.getValue();

        if (type.equals(EventType.ElementOpen)) {
            if (states.get(docId) == null) {
                // Create the base state
                boolean[] state = new boolean[expressions.length];
                for (int i = 0; i < expressions.length; i++) {
                    state[i] = false;
                }
                states.put(docId, state);
                
                // Initialize the list of nodes
                ArrayList<XPathNode> nodeList = new ArrayList<>();
                nodeList.add(new XPathNode(value, false));
                nodes.put(docId, nodeList);
            } else {
                // Add a node
                List<XPathNode> nodeList = nodes.get(docId);
                nodeList.add(new XPathNode(value, false));
                nodes.put(docId, nodeList);
            }
            updateState(docId);

        } else if (type.equals(EventType.ElementClose)) {
            List<XPathNode> nodeList = nodes.get(docId);
            if (nodeList != null) {
                
                // Delete the last node
                XPathNode lastNode = nodeList.get(nodeList.size() - 1);
                if (lastNode.getValue().equals(value)) {
                    nodeList.remove(lastNode);
                }
                if (nodeList.isEmpty()) {
                    nodes.remove(nodeList);
                }
            }

        } else if (type.equals(EventType.Text)) {
            List<XPathNode> nodeList = nodes.get(docId);
            if (nodeList != null) {
                
                // Add text to the node
                XPathNode lastNode = nodeList.get(nodeList.size() - 1);
                lastNode.setText(value);
                
                updateState(docId);
            }
        }
        return states.get(docId);
    }

    @Override
    public String toString() {
        String res = "";
        for (XPathNode node : expressionNodes.get(0)) {
            res += "/" + node.toString();
        }
        return res;
    }
}
