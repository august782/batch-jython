package org.python.compiler;

import batch.partition.ExtraInfo;

import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;

public abstract class Generator implements ExtraInfo<Generator> {
    
    Object extraKey;
    Object extraInfo;
    
    public Generator setExtra(Object extraKey, Object extraInfo) {
        this.extraKey = extraKey;
        this.extraInfo = extraInfo;
        return this;
    }
    
    abstract public expr generateExpr();
    
    abstract public mod generateMod();
    
    abstract public stmt generateStmt();
    
    abstract public expr generateRemote();
}
