package frontend;

import error.Error;
import error.ErrorHandler;
import error.ErrorType;
import node.ExpAlikeNode;
import node.PseudoNode;
import node.nodes.*;
import node.pseudoNodes.*;
import token.Token;
import token.TokenType;
import utils.FileIO;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Centralized Parser
 */
public class Parser {
    private final ArrayList<Token> tokenList;
    private final int maxLength;
    private int pos;
    private Token curToken;
    private CompUnitNode rootNode = null;
    private boolean inPin = false;

    public CompUnitNode getRootNode() {
        return rootNode;
    }

    public Parser(ArrayList<Token> tokenList) {
        this.tokenList = tokenList;
        this.maxLength = tokenList.size();
        this.pos = 0;
        this.curToken = tokenList.get(pos);
    }

    public void parse() {
        if (tokenList.isEmpty()) {
            throw new RuntimeException("WARNING: parser gets an empty tokenList");
        }
        rootNode = parseCompUnit();
    }

    /**
     * match token with expected TokenType, handle error i,j,k if needed
     * @param tokenType expected TokenType
     * @return matched token, return null if error i,j,k
     */
    private Token matchToken(TokenType tokenType) {
        if (curToken.getTokenType() == tokenType) {
            Token tmp = curToken;
            if (pos < maxLength) {
                pos++;
                curToken = tokenList.get(pos);
            } else {
                throw new RuntimeException("WARNING: matching end");
            }
            return tmp;
        } else if (tokenType == TokenType.SEMICN || tokenType == TokenType.RPARENT || tokenType == TokenType.RBRACK) {
            int lineNum = tokenList.get(pos - 1).getLineNum();
            String str = tokenType.getValue();
            ErrorType errorType = switch (tokenType) {
                case SEMICN -> ErrorType.i;
                case RPARENT -> ErrorType.j;
                case RBRACK -> ErrorType.k;
                default -> null;
            };
            Error error = new Error(errorType, lineNum);
            if (!inPin) {
                ErrorHandler.addError(error);
            }
            return new Token(tokenType.getValue(), tokenType, lineNum);
        }
        throw new RuntimeException("WARNING: can't match " + tokenType);
    }

    private boolean peek(int dis, TokenType tokenType) {
        if (pos + dis >= maxLength) {
            return false;
        }
        Token peekToken = tokenList.get(pos + dis);
        return peekToken.getTokenType().equals(tokenType);
    }

    public static ArrayList<TokenType> potentialExpFirstToken = new ArrayList<>(Arrays.asList(
            TokenType.IDENFR, TokenType.PLUS, TokenType.MINU, TokenType.NOT, TokenType.LPARENT, TokenType.INTCON));

    /**
     * when matching function calls, it would be tricky if the right parenthesis of real parameters list is absent
     * @param tokenType token to check
     * @return whether it is potentially in an expression
     */
    public static boolean isPotentialExp(TokenType tokenType) {
        return potentialExpFirstToken.contains(tokenType);
    }

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    // Decl → ConstDecl | VarDecl
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    // MainFuncDef → 'int' 'main' '(' ')' Block
    private CompUnitNode parseCompUnit() {
        ArrayList<DeclNode> declNodes = new ArrayList<>();
        ArrayList<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode;

        // Decl   if the second token after is '('
        while (!peek(2, TokenType.LPARENT)) {
            declNodes.add(parseDecl());
        }

        //FuncDef
        while (!peek(1, TokenType.MAINTK)) {
            funcDefNodes.add(parseFuncDef());
        }

        // MainFuncDef
        mainFuncDefNode = parseMainFuncDef();
        return new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);
    }

    // Decl → ConstDecl | VarDecl
    private DeclNode parseDecl() {
        ConstDeclNode constDeclNode = null;
        VarDeclNode varDeclNode = null;
        if (curToken.getTokenType() == TokenType.CONSTTK) {
            constDeclNode = parseConstDecl();
        } else {
            varDeclNode = parseVarDecl();
        }
        return new DeclNode(constDeclNode, varDeclNode);
    }

    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDefNode parseFuncDef() {
        FuncTypeNode funcTypeNode = parseFuncType();
        Token identToken = matchToken(TokenType.IDENFR);
        Token lParent = matchToken(TokenType.LPARENT);
        FuncFParamsNode funcFParamsNode = null;
        if (curToken.getTokenType() == TokenType.INTTK) {
            funcFParamsNode = parseFuncFParams();
        }
        Token rParent = matchToken(TokenType.RPARENT);
        BlockNode blockNode = parseBlock();
        return new FuncDefNode(funcTypeNode, identToken, lParent, funcFParamsNode, rParent, blockNode);
    }

    // MainFuncDef → int main ( ) Block
    private MainFuncDefNode parseMainFuncDef() {
        Token intToken = matchToken(TokenType.INTTK);
        Token mainToken = matchToken(TokenType.MAINTK);
        Token lParent = matchToken(TokenType.LPARENT);
        Token rParent = matchToken(TokenType.RPARENT);
        BlockNode blockNode = parseBlock();
        return new MainFuncDefNode(intToken, mainToken, lParent, rParent, blockNode);
    }

    //ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDeclNode parseConstDecl() {
        Token constToken = matchToken(TokenType.CONSTTK);
        BTypeNode bTypeNode = parseBType();
        ArrayList<Token> commas = new ArrayList<>();
        ArrayList<ConstDefNode> constDefNodes = new ArrayList<>();
        constDefNodes.add(parseConstDef());
        while (curToken.getTokenType() == TokenType.COMMA) {
            commas.add(matchToken(TokenType.COMMA));
            constDefNodes.add(parseConstDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new ConstDeclNode(constToken, bTypeNode, constDefNodes, commas, semicnToken);
    }

    //  VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
    private VarDeclNode parseVarDecl() {
        Token staticToken = null;
        if (curToken.getTokenType() == TokenType.STATICTK) {
            staticToken = matchToken(TokenType.STATICTK);
        }
        BTypeNode bTypeNode = parseBType();
        ArrayList<VarDefNode> varDefNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        varDefNodes.add(parseVarDef());
        while (curToken.getTokenType() == TokenType.COMMA) {
            commas.add(matchToken(TokenType.COMMA));
            varDefNodes.add(parseVarDef());
        }
        Token semicnToken = matchToken(TokenType.SEMICN);
        return new VarDeclNode(staticToken, bTypeNode, varDefNodes, commas, semicnToken);
    }

    //BType → 'int'
    private BTypeNode parseBType() {
        Token token = null;
        token = matchToken(TokenType.INTTK);

        return new BTypeNode(token);
    }

    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
    private ConstDefNode parseConstDef() {
        Token identToken = matchToken(TokenType.IDENFR);
        Token lBrack = null;
        ConstExpNode constExpNode = null;
        Token rBrack = null;
        if (curToken.getTokenType() == TokenType.LBRACK) {
            lBrack = matchToken(TokenType.LBRACK);
            constExpNode = parseConstExp();
            rBrack = matchToken(TokenType.RBRACK);
        }
        Token assignToken = matchToken(TokenType.ASSIGN);
        ConstInitValNode constInitValNode = parseConstInitVal();
        return new ConstDefNode(identToken, lBrack, constExpNode, rBrack, assignToken, constInitValNode);
    }

    // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    private VarDefNode parseVarDef() {
        Token identToken = matchToken(TokenType.IDENFR);
        Token lBracketToken = null;
        ConstExpNode constExpNode = null;
        Token rBracketToken = null;
        Token assignToken = null;
        InitValNode initValNode = null;
        if (curToken.getTokenType() == TokenType.LBRACK) {
            lBracketToken = matchToken(TokenType.LBRACK);
            constExpNode = parseConstExp();
            rBracketToken = matchToken(TokenType.RBRACK);
        }
        if (curToken.getTokenType() == TokenType.ASSIGN) {
            assignToken = matchToken(TokenType.ASSIGN);
            initValNode = parseInitVal();
        }
        return new VarDefNode(identToken, lBracketToken, constExpNode, rBracketToken, assignToken, initValNode);
    }

    // ConstExp → AddExp
    private ConstExpNode parseConstExp() {
        AddExpNode addExpNode = parseAddExp();
        return new ConstExpNode(addExpNode);
    }

    //Exp → AddExp
    private ExpNode parseExp() {
        AddExpNode addExpNode = parseAddExp();
        return new ExpNode(addExpNode);
    }

    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
    private ConstInitValNode parseConstInitVal() {
        ConstExpNode constExpNode = null;
        Token lBrace = null;
        ArrayList<ExpAlikeNode> constExpNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        Token rBrace = null;

        if (curToken.getTokenType() == TokenType.LBRACE) {
            lBrace = matchToken(TokenType.LBRACE);
            if (curToken.getTokenType() != TokenType.RBRACE) {
                constExpNodes.add(parseConstExp());
                while (curToken.getTokenType() == TokenType.COMMA) {
                    commas.add(matchToken(TokenType.COMMA));
                    constExpNodes.add(parseConstExp());
                }
            }
            rBrace = matchToken(TokenType.RBRACE);
        }  else {
            constExpNodes.add(parseConstExp());
        }
        return new ConstInitValNode(lBrace, constExpNodes, commas, rBrace);
    }

    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
    private InitValNode parseInitVal() {
        Token lBrace = null;
        ArrayList<ExpAlikeNode> expNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        Token rBrace = null;

        if (curToken.getTokenType() == TokenType.LBRACE) {
            lBrace = matchToken(TokenType.LBRACE);
            if (curToken.getTokenType() != TokenType.RBRACE) {
                expNodes.add(parseExp());
                while (curToken.getTokenType() == TokenType.COMMA) {
                    commas.add(matchToken(TokenType.COMMA));
                    expNodes.add(parseExp());
                }
            }
            rBrace = matchToken(TokenType.RBRACE);
        }  else {
            expNodes.add(parseExp());
        }
        return new InitValNode(lBrace, expNodes, commas, rBrace);
    }

    // FuncType → 'void' | 'int'
    private FuncTypeNode parseFuncType() {
        Token type;
        if (curToken.getTokenType() == TokenType.INTTK) {
            type = matchToken(TokenType.INTTK);
        } else {
            type = matchToken(TokenType.VOIDTK);
        }
        return new FuncTypeNode(type);
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    private FuncFParamsNode parseFuncFParams() {
        ArrayList<FuncFParamNode> funcFParamNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        funcFParamNodes.add(parseFuncFParam());
        while (curToken.getTokenType() == TokenType.COMMA) {
            commas.add(matchToken(TokenType.COMMA));
            funcFParamNodes.add(parseFuncFParam());
        }
        return new FuncFParamsNode(funcFParamNodes, commas);
    }

    // FuncFParam → BType Ident ['[' ']']
    private FuncFParamNode parseFuncFParam() {
        BTypeNode bTypeNode = parseBType();
        Token identToken = matchToken(TokenType.IDENFR);
        Token lBrack = null;
        Token rBrack = null;
        if (curToken.getTokenType() == TokenType.LBRACK) {
            lBrack = matchToken(TokenType.LBRACK);
            rBrack = matchToken(TokenType.RBRACK);
        }
        return new FuncFParamNode(bTypeNode, identToken, lBrack, rBrack);
    }

    // Block → '{' { BlockItem } '}'
    private BlockNode parseBlock() {
        Token lbraceToken = matchToken(TokenType.LBRACE);
        ArrayList<BlockItemNode> blockItemNodes = new ArrayList<>();
        Token rbraceToken = null;
        while (curToken.getTokenType() != TokenType.RBRACE) {
            blockItemNodes.add(parseBlockItem());
        }
        rbraceToken = matchToken(TokenType.RBRACE);
        return new BlockNode(lbraceToken, blockItemNodes, rbraceToken);
    }

    // BlockItem → Decl | Stmt
    private BlockItemNode parseBlockItem() {
        DeclNode declNode = null;
        StmtNode stmtNode = null;

        if (curToken.getTokenType() == TokenType.CONSTTK || curToken.getTokenType() == TokenType.INTTK || curToken.getTokenType() == TokenType.STATICTK) {
            declNode = parseDecl();
        } else {
            stmtNode = parseStmt();
        }
        return new BlockItemNode(declNode, stmtNode);
    }

    /* Stmt → LVal '=' Exp ';' // i
            | [Exp] ';' // i
            | Block
            | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
            | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            | 'break' ';' | 'continue' ';' // i
            | 'return' [Exp] ';' // i
            | 'printf''('StringConst {','Exp}')'';' // i j
    */
    private StmtNode parseStmt() {
        PseudoNode pseudoNode = null;

        if (curToken.getTokenType() == TokenType.LBRACE) {// Block
            BlockNode blockNode = parseBlock();
            return new StmtNode(null, blockNode, null, null);
        } else if (curToken.getTokenType() == TokenType.IFTK) {
            pseudoNode = parseIfStmt();
            return new StmtNode(pseudoNode, null, null, null);
        } else if (curToken.getTokenType() == TokenType.FORTK) {
            pseudoNode = parseFor();
            return new StmtNode(pseudoNode, null, null, null);
        } else if (curToken.getTokenType() == TokenType.RETURNTK) {
            pseudoNode = parseReturnStmt();
            return new StmtNode(pseudoNode, null, null, null);
        } else if (curToken.getTokenType() == TokenType.PRINTFTK) {
            pseudoNode = parsePrintfStmt();
            return new StmtNode(pseudoNode, null, null, null);
        } else if (curToken.getTokenType() == TokenType.BREAKTK) {
            Token singleBranchToken = matchToken(TokenType.BREAKTK);
            Token semiToken = matchToken(TokenType.SEMICN);
            return new StmtNode(null, null, singleBranchToken, semiToken);
        } else if (curToken.getTokenType() == TokenType.CONTINUETK) {
            Token singleBranchToken = matchToken(TokenType.CONTINUETK);
            Token semiToken = matchToken(TokenType.SEMICN);
            return new StmtNode(null, null, singleBranchToken, semiToken);
        } else if (curToken.getTokenType() == TokenType.SEMICN) { //  ExpStmtNode without Exp
            Token semiToken = matchToken(TokenType.SEMICN);
            return new StmtNode(new ExpStmtNode(null, semiToken), null, null, null);
        }

        //      LVal '=' Exp ';'
        //    | [Exp] ';' // Exp must exist
        int pinPos = pos; // pin
        inPin = true;
        ExpNode expNode = parseExp();
        if (curToken.getTokenType() == TokenType.ASSIGN) {
            pos = pinPos;
            inPin = false;
            curToken = tokenList.get(pos);
            LValAssignStmtNode lValAssignStmtNode = parseLValAssignStmt();
            return new StmtNode(lValAssignStmtNode, null, null, null);
        } else {
            pos = pinPos;
            inPin = false;
            curToken = tokenList.get(pos);
            ExpStmtNode expStmtNode = parseExpStmt();
            return new StmtNode(expStmtNode, null, null, null);
        }
    }

    // LVal → Ident ['[' Exp ']']
    private LValNode parseLVal() {
        Token identToken = matchToken(TokenType.IDENFR);
        Token lBrack = null;
        ExpNode expNode = null;
        Token rBrack = null;

        if (curToken.getTokenType() == TokenType.LBRACK) {
            lBrack = matchToken(TokenType.LBRACK);
            expNode = parseExp();
            rBrack = matchToken(TokenType.RBRACK);
        }

        return new LValNode(identToken, lBrack, expNode, rBrack);
    }

    private LValAssignStmtNode parseLValAssignStmt() {
        LValNode lValNode = parseLVal();
        Token assingToken = matchToken(TokenType.ASSIGN);
        ExpNode expNode = parseExp();
        Token semiToken = matchToken(TokenType.SEMICN);
        return new LValAssignStmtNode(lValNode, assingToken, expNode, semiToken);
    }

    // Exp must exist
    private ExpStmtNode parseExpStmt() {
        ExpNode expNode = parseExp();
        Token semiToken = matchToken(TokenType.SEMICN);
        return new ExpStmtNode(expNode, semiToken);
    }

    private IfStmtNode parseIfStmt() {
        Token ifToken = matchToken(TokenType.IFTK);
        Token lParent = matchToken(TokenType.LPARENT);
        CondNode condNode = parseCond();
        Token rParent = matchToken(TokenType.RPARENT);
        StmtNode stmtNodeIf = parseStmt();
        Token elseToken = null;
        StmtNode stmtNodeElse = null;

        if (curToken.getTokenType() == TokenType.ELSETK) {
            elseToken = matchToken(TokenType.ELSETK);
            stmtNodeElse = parseStmt();
        }

        return new IfStmtNode(ifToken, lParent, condNode, rParent, stmtNodeIf, elseToken, stmtNodeElse);
    }

    private ForNode parseFor() {
        Token forToken = matchToken(TokenType.FORTK);
        Token lParentToken = matchToken(TokenType.LPARENT);
        ForStmtNode forStmtNode1 = null;
        Token semiToken1 = null;
        CondNode condNode = null;
        Token semiToken2 = null;
        ForStmtNode forStmtNode2 = null;
        Token rParentToken = null;
        StmtNode stmtNode = null;

        if (curToken.getTokenType() != TokenType.SEMICN) {
            forStmtNode1 = parseForStmt();
        }
        semiToken1 = matchToken(TokenType.SEMICN);
        if (curToken.getTokenType() != TokenType.SEMICN) {
            condNode = parseCond();
        }
        semiToken2 = matchToken(TokenType.SEMICN);
        if (curToken.getTokenType() != TokenType.RPARENT) {
            forStmtNode2 = parseForStmt();
        }
        rParentToken = matchToken(TokenType.RPARENT);

        stmtNode = parseStmt();

        return new ForNode(forToken, lParentToken, forStmtNode1, semiToken1,
                condNode, semiToken2, forStmtNode2, rParentToken, stmtNode);
    }

    private CondNode parseCond() {
        LOrExpNode lOrExpNode = parseLOrExp();
        return new CondNode(lOrExpNode);
    }

    private ForStmtNode parseForStmt() {
        ArrayList<LValNode> lValNodes = new ArrayList<>();
        ArrayList<Token> assignTokens = new ArrayList<>();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> commaTokens = new ArrayList<>();

        lValNodes.add(parseLVal());
        assignTokens.add(matchToken(TokenType.ASSIGN));
        expNodes.add(parseExp());
        while (curToken.getTokenType() == TokenType.COMMA) {
            commaTokens.add(matchToken(TokenType.COMMA));
            lValNodes.add(parseLVal());
            assignTokens.add(matchToken(TokenType.ASSIGN));
            expNodes.add(parseExp());
        }
        return  new ForStmtNode(lValNodes, assignTokens, expNodes, commaTokens);
    }

    private ReturnStmtNode parseReturnStmt() {
        Token returnToken = matchToken(TokenType.RETURNTK);
        ExpNode expNode = null;
        Token semiToken = null;

        if (curToken.getTokenType() == TokenType.SEMICN) {
            semiToken = matchToken(TokenType.SEMICN);
        } else {
            expNode = parseExp();
            semiToken = matchToken(TokenType.SEMICN);
        }


        return new ReturnStmtNode(returnToken, expNode, semiToken);
    }

    private PrintfStmtNode parsePrintfStmt() {
        Token printfToken = matchToken(TokenType.PRINTFTK);
        Token lParent = matchToken(TokenType.LPARENT);
        Token stringConstToken = matchToken(TokenType.STRCON);
        ArrayList<Token> commaTokens = new ArrayList<>();
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        Token rParent = null;
        Token semiToken = null;

        while (curToken.getTokenType() == TokenType.COMMA) {
            commaTokens.add(matchToken(TokenType.COMMA));
            expNodes.add(parseExp());
        }
        rParent = matchToken(TokenType.RPARENT);
        semiToken = matchToken(TokenType.SEMICN);
        return new PrintfStmtNode(printfToken, lParent, stringConstToken, commaTokens, expNodes, rParent, semiToken);
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    private LOrExpNode parseLOrExp() {
        LOrExpNode lOrExpNode = null;
        Token orToken = null;
        LAndExpNode lAndExpNode = parseLAndExp();

        while (curToken.getTokenType() == TokenType.OR) {
            lOrExpNode = new LOrExpNode(lOrExpNode, orToken, lAndExpNode);
            orToken = matchToken(curToken.getTokenType());
            lAndExpNode = parseLAndExp();
        }
        return new LOrExpNode(lOrExpNode, orToken, lAndExpNode);
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp
    private LAndExpNode parseLAndExp() {
        LAndExpNode lAndExpNode = null;
        Token andToken = null;
        EqExpNode eqExpNode = parseEqExp();

        while (curToken.getTokenType() == TokenType.AND) {
            lAndExpNode = new LAndExpNode(lAndExpNode, andToken, eqExpNode);
            andToken = matchToken(curToken.getTokenType());
            eqExpNode = parseEqExp();
        }
        return new LAndExpNode(lAndExpNode, andToken, eqExpNode);
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    private EqExpNode parseEqExp() {
        EqExpNode eqExpNode = null;
        Token judgeToken = null;
        RelExpNode relExpNode = RelExp();

        while (curToken.getTokenType() == TokenType.EQL || curToken.getTokenType() == TokenType.NEQ) {
            eqExpNode = new EqExpNode(eqExpNode, judgeToken, relExpNode);
            judgeToken = matchToken(curToken.getTokenType());
            relExpNode = RelExp();
        }
        return new EqExpNode(eqExpNode, judgeToken, relExpNode);
    }

    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    private RelExpNode RelExp() {
        RelExpNode relExpNode = null;
        Token relToken = null;
        AddExpNode addExpNode = parseAddExp();

        while (curToken.getTokenType() == TokenType.LSS || curToken.getTokenType() == TokenType.GRE || curToken.getTokenType() == TokenType.LEQ || curToken.getTokenType() == TokenType.GEQ) {
            relExpNode = new RelExpNode(relExpNode, relToken, addExpNode);
            relToken = matchToken(curToken.getTokenType());
            addExpNode = parseAddExp();
        }
        return new RelExpNode(relExpNode, relToken, addExpNode);
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    private AddExpNode parseAddExp() {
        AddExpNode addExpNode = null;
        Token opToken = null;
        MulExpNode mulExpNode = parseMulExp();

        while (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU) {
            addExpNode = new AddExpNode(addExpNode, opToken, mulExpNode);
            opToken = matchToken(curToken.getTokenType());
            mulExpNode = parseMulExp();
        }
        return new AddExpNode(addExpNode, opToken, mulExpNode);
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    private MulExpNode parseMulExp() {
        UnaryExpNode unaryExpNode = parseUnaryExp();
        Token opToken = null;
        MulExpNode mulExpNode = null;

        while (curToken.getTokenType() == TokenType.MULT || curToken.getTokenType() == TokenType.DIV || curToken.getTokenType() == TokenType.MOD) {
            mulExpNode = new MulExpNode(unaryExpNode, opToken, mulExpNode);
            opToken = matchToken(curToken.getTokenType());
            unaryExpNode = parseUnaryExp();
        }

        return new MulExpNode(unaryExpNode, opToken, mulExpNode);
    }

    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private UnaryExpNode parseUnaryExp() {
        PrimaryExpNode primaryExpNode = null;
        Token identToken = null;
        Token lParent = null;
        FuncRParamsNode funcRParamsNode = null;
        Token rParent = null;
        UnaryOpNode unaryOpNode = null;
        UnaryExpNode unaryExpNode = null;
        if (curToken.getTokenType() == TokenType.IDENFR && peek(1, TokenType.LPARENT)) {
            identToken = matchToken(TokenType.IDENFR);
            lParent = matchToken(TokenType.LPARENT);
            if (isPotentialExp(curToken.getTokenType())) {
                funcRParamsNode = parseFuncRParams();
            }
            rParent = matchToken(TokenType.RPARENT);
        } else if (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU || curToken.getTokenType() == TokenType.NOT) {
            unaryOpNode = parseUnaryOp();
            unaryExpNode = parseUnaryExp();
        } else {
            primaryExpNode = parsePrimaryExp();
        }
        return new UnaryExpNode(primaryExpNode, identToken, lParent, funcRParamsNode, rParent, unaryOpNode, unaryExpNode);
    }

    // FuncRParams → Exp { ',' Exp }
    private FuncRParamsNode parseFuncRParams() {
        ArrayList<ExpNode> expNodes = new ArrayList<>();
        ArrayList<Token> commas = new ArrayList<>();
        expNodes.add(parseExp());
        while (curToken.getTokenType() == TokenType.COMMA) {
            commas.add(matchToken(TokenType.COMMA));
            expNodes.add(parseExp());
        }
        return new FuncRParamsNode(expNodes, commas);
    }

    // UnaryOp → '+' | '−' | '!'
    private UnaryOpNode parseUnaryOp() {
        Token opToken;
        if (curToken.getTokenType() == TokenType.PLUS || curToken.getTokenType() == TokenType.MINU || curToken.getTokenType() == TokenType.NOT) {
            opToken = matchToken(curToken.getTokenType());
        } else {
            // default as plus
            opToken = matchToken(TokenType.PLUS);
        }
        return new UnaryOpNode(opToken);
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number
    private PrimaryExpNode parsePrimaryExp() {
        Token lParent = null;
        ExpNode expNode = null;
        Token rParent = null;
        LValNode lValNode = null;
        NumberNode numberNode = null;

        if (curToken.getTokenType() == TokenType.LPARENT) {
            lParent = matchToken(TokenType.LPARENT);
            expNode = parseExp();
            rParent = matchToken(TokenType.RPARENT);
        } else if (curToken.getTokenType() == TokenType.INTCON) {
            numberNode = parseNumber();
        } else {
            lValNode = parseLVal();
        }
        return new PrimaryExpNode(lParent, expNode, rParent, lValNode, numberNode);
    }

    private NumberNode parseNumber() {
        Token intConstToken = matchToken(TokenType.INTCON);
        return new NumberNode(intConstToken);
    }

    public void outputAST() {
        String result = rootNode.toString();
        FileIO.write(FileIO.IOType.PARSER, result);
    }
}
