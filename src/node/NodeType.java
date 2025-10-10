package node;

public enum NodeType {
    // compile-unit
    CompUnit,

    // declarations
    Decl,
    ConstDecl,
    BType,
    ConstDef,
    ConstInitVal,
    VarDecl,
    VarDef,
    InitVal,

    // functions
    FuncDef,
    MainFuncDef,
    FuncType,
    FuncFParams,
    FuncFParam,

    // blocks
    Block,
    BlockItem,

    // statements
    Stmt,
    ForStmt,

    // expressions
    Exp,
    Cond,
    LVal,
    PrimaryExp,
    Number,
    UnaryExp,
    UnaryOp,
    FuncRParams,
    MulExp,
    AddExp,
    RelExp,
    EqExp,
    LAndExp,
    LOrExp,
    ConstExp
}
