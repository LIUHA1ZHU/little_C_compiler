package error;

public enum ErrorType {
    a, // Illegal token | &
    b, // Redefined name
    c, // Undefined name
    d, // Parameter count mismatch
    e, // Parameter type mismatch
    f, // Return mismatch in void function
    g, // Missing return in non-void function
    h, // Modify constant value
    i, // Missing semicolon
    j, // Missing right parenthesis
    k, // Missing right bracket
    l, // Printf format mismatch
    m  // Break/continue outside loop
}
