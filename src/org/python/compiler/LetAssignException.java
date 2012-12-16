package org.python.compiler;

import org.python.antlr.base.expr;

import batch.partition.*;

public class LetAssignException extends Exception {
    
    private java.util.List<expr> vars;
    private expr value;
    
    public LetAssignException(java.util.List<expr> vars, expr value) {
        this.vars = vars;
        this.value = value;
    }
    
    public java.util.List<expr> getVars() {
        return vars;
    }
    
    public expr getValue() {
        return value;
    }
}
