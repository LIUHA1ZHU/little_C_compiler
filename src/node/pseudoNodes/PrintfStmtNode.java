package node.pseudoNodes;

import node.PseudoNode;
import node.PseudoNodeType;
import node.nodes.ExpNode;
import token.Token;

import java.util.ArrayList;

/**
 * 'printf''('StringConst { ','Exp } ')'';'
 */
public class PrintfStmtNode extends PseudoNode {
    private final Token printfToken;
    private final Token lParent;
    private final Token stringConstToken;
    private final ArrayList<Token> commaTokens;
    private final ArrayList<ExpNode> expNodes;
    private final Token rParent;
    private final Token semiToken;

    public PrintfStmtNode(Token printfToken, Token lParent, Token stringConstToken, ArrayList<Token> commaTokens,
                          ArrayList<ExpNode> expNodes, Token rParent, Token semiToken) {
        super(PseudoNodeType.PrintfStmt);
        this.printfToken = printfToken;
        this.lParent = lParent;
        this.stringConstToken = stringConstToken;
        this.commaTokens = commaTokens;
        this.expNodes = expNodes;
        this.rParent = rParent;
        this.semiToken = semiToken;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(printfToken).append(lParent).append(stringConstToken);
        for (int i = 0; i < commaTokens.size(); i++) {
            sb.append(commaTokens.get(i)).append(expNodes.get(i));
        }
        sb.append(rParent).append(semiToken);
        return sb.toString();
    }
}
