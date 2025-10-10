package node;

public abstract class PseudoNode {
    private PseudoNodeType pseudoNodeType;

    public PseudoNode(PseudoNodeType type) {
        this.pseudoNodeType = type;
    }

    public abstract String toString();
}
