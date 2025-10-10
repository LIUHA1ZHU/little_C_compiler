package node;

public abstract class Node {
    private NodeType nodeType;

    public Node(NodeType type) {
        this.nodeType = type;
    }

    public abstract String toString();

    // in parser output form
    public String printNodeType() {
        return "<" + nodeType + ">" + "\n";
    }
}
