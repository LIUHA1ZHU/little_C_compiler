package midEnd.visitor;

import midEnd.ir.IrBuilder;
import midEnd.symbol.SymbolManager;
import node.nodes.BlockItemNode;
import node.nodes.BlockNode;

public class BlockVisitor {

    public static void visit(BlockNode blockNode) {
        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            visitBlockItem(blockItemNode);
        }
    }

    public static void visitBlockItem(BlockItemNode blockItemNode) {
        if (blockItemNode.getDeclNode() != null) {
            DeclVisitor.visit(blockItemNode.getDeclNode());
        } else {
            StmtVisitor.visit(blockItemNode.getStmtNode());
        }
    }
}
