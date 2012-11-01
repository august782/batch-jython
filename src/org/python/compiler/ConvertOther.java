// Copyright (c) Corporation for National Research Initiatives
package org.python.compiler;

import org.python.antlr.adapter.AstAdapters;
import org.python.core.AstList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.python.antlr.ParseException;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.Assert;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.AugAssign;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.BoolOp;
import org.python.antlr.ast.Break;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Compare;
import org.python.antlr.ast.Continue;
import org.python.antlr.ast.Delete;
import org.python.antlr.ast.Dict;
import org.python.antlr.ast.Ellipsis;
import org.python.antlr.ast.ExceptHandler;
import org.python.antlr.ast.Exec;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.ExtSlice;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.GeneratorExp;
import org.python.antlr.ast.Global;
import org.python.antlr.ast.If;
import org.python.antlr.ast.IfExp;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Index;
import org.python.antlr.ast.Interactive;
import org.python.antlr.ast.Lambda;
import org.python.antlr.ast.List;
import org.python.antlr.ast.ListComp;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Pass;
import org.python.antlr.ast.Print;
import org.python.antlr.ast.Raise;
import org.python.antlr.ast.Repr;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Slice;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.Subscript;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.TryExcept;
import org.python.antlr.ast.TryFinally;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.UnaryOp;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.cmpopType;
import org.python.antlr.ast.comprehension;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.ast.keyword;
import org.python.antlr.ast.operatorType;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.antlr.base.stmt;
import org.python.core.CompilerFlags;
import org.python.core.ContextGuard;
import org.python.core.ContextManager;
import org.python.core.imp;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyComplex;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyFrame;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PySet;
import org.python.core.PySlice;
import org.python.core.PyString;
import org.python.core.PyTuple;
import org.python.core.PyUnicode;
import org.python.core.ThreadState;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import static org.python.util.CodegenUtils.*;

import batch.partition.*;
import batch.Op;

public class ConvertOther extends Visitor {
    
    private java.util.List<Generator> subs;
    private PartitionFactoryHelper<Generator> helper;
    
    public ConvertOther(java.util.List<Generator> subs, PartitionFactoryHelper<Generator> helper) {
        this.subs = subs;
        this.helper = helper;
    }
    
    // Helper functions
    private mod makeMod(stmt s) {
        // Make mod by wrapping inside suite ??
        java.util.List<stmt> body = new java.util.ArrayList<stmt>();
        body.add(s);
        return  new Suite(new AstList(body, AstAdapters.stmtAdapter));
    }
    // End helper functions
    
    @Override
    public Object visitPrint(final Print node) {
        expr dest = subs.get(0).generateExpr();
        java.util.List<expr> values = ((List)(subs.get(1)).generateExpr()).getInternalElts();
        // Fake as integer
        Integer temp_nl = new Integer(((PyInteger)((Num)(subs.get(2)).generateExpr()).getInternalN()).getValue());
        PyObject nl;
        if (temp_nl == 1) {
            nl = Py.True; 
        }
        else {
            nl = Py.False;
        }
        node.setDest(dest);
        node.setValues(new AstList(values, AstAdapters.exprAdapter));
        node.setNl(nl); // Seems to need real Boolean...
        
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Print
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return node;
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    
    @Override
    public Object visitDelete(final Delete node) {
        java.util.List<expr> targets = ((List)(subs.get(0).generateExpr())).getInternalElts();
        node.setTargets(new AstList(targets, AstAdapters.exprAdapter));
        
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Delete
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return node;
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    
    @Override
    public Object visitPass(final Pass node) {
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Pass
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return node;
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    
    @Override
    public Object visitBreak(final Break node) {
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Break
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return node;
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    
    @Override
    public Object visitYield(final Yield node) {
        expr value = subs.get(0).generateExpr();
        node.setValue(value);
        
        return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(node);
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    
    @Override
    public Object visitWhile(final While node) {
        expr test = subs.get(0).generateExpr();
        java.util.List<stmt> body = ((Suite)subs.get(1).generateMod()).getInternalBody();
        java.util.List<stmt> orelse = ((Suite)subs.get(2).generateMod()).getInternalBody();
        node.setTest(test);
        node.setBody(new AstList(body, AstAdapters.stmtAdapter));
        node.setOrelse(new AstList(orelse, AstAdapters.stmtAdapter));
        
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for While
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return node;
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
}
