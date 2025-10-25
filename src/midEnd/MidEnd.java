package midEnd;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import midEnd.symbol.FuncSymbol;
import midEnd.symbol.Symbol;
import midEnd.symbol.SymbolManager;
import midEnd.visitor.BlockVisitor;
import midEnd.visitor.DeclVisitor;
import midEnd.visitor.FuncDefVisitor;
import node.nodes.CompUnitNode;
import node.nodes.DeclNode;
import node.nodes.FuncDefNode;

import java.util.ArrayList;

/**
 * go through the syntax tree ONCE, create symbolTable and generate intermediate representation if there's no error
 */
public class MidEnd {
    private CompUnitNode rootNode;

    public MidEnd(CompUnitNode rootNode) {
        this.rootNode = rootNode;
    }

    public void visit() {
        for (DeclNode declNode : rootNode.getDeclNodes()) {
            DeclVisitor.visit(declNode);
        }
        for (FuncDefNode funcDefNode : rootNode.getFuncDefNodes()) {
            FuncDefVisitor.visit(funcDefNode);
        }
        // MainFuncDef
        SymbolManager.createTableAndChangeCur(new FuncSymbol("main", Symbol.SymbolType.IntFunc,
                rootNode.getMainFuncDefNode().getMainToken().getLineNum(), new ArrayList<>()), false);
        BlockVisitor.visit(rootNode.getMainFuncDefNode().getBlockNode());
        if (!SymbolManager.getLastIsReturn()) {
            ErrorHandler.addError(new Error(ErrorType.g, rootNode.getMainFuncDefNode().getBlockNode().getrBrace().getLineNum()));
        }
        SymbolManager.goToFatherTable();

        SymbolManager.end();
    }
}
