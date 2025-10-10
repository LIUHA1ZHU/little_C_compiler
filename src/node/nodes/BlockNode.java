package node.nodes;

import node.Node;
import node.NodeType;
import token.Token;

import java.util.ArrayList;

/**
 * Block → '{' { BlockItem } '}'
 */
public class BlockNode extends Node {
    private final Token lBrace;
    private final ArrayList<BlockItemNode> blockItemNodes;
    private final Token rBrace;

    public BlockNode(Token lBrace, ArrayList<BlockItemNode> blockItemNodes, Token rBrace) {
        super(NodeType.Block);
        this.lBrace = lBrace;
        this.blockItemNodes = blockItemNodes;
        this.rBrace = rBrace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lBrace);
        if (blockItemNodes != null) {
            for (BlockItemNode blockItemNode : blockItemNodes) {
                sb.append(blockItemNode);
            }
        }
        sb.append(rBrace);
        return sb.append(printNodeType()).toString();
    }
}
