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
    
    //private java.util.List<Generator> subs;
    private java.util.List<mod> subs;
    //private PartitionFactoryHelper<Generator> helper;
    
    //public ConvertOther(java.util.List<Generator> subs, PartitionFactoryHelper<Generator> helper) {
    public ConvertOther(java.util.List<mod> subs) {
        this.subs = subs;
    }
    
    // Helper functions
    /*private mod makeMod(stmt s) {
        // Make mod by wrapping inside suite ??
        java.util.List<stmt> body = new java.util.ArrayList<stmt>();
        body.add(s);
        return new Suite(new AstList(body, AstAdapters.stmtAdapter));
    }*/
    private expr makeExpr(mod m) {
        Expr e = (Expr)makeStmt(m);
        if (e == null) {    // Propogate
            return null;
        }
        return e.getInternalValue();
    }
    
    private stmt makeStmt(mod m) {
        Suite s = (Suite)m;
        if (s.getInternalBody().size() == 0) {  // Body empty?
            return null;
        }
        return s.getInternalBody().get(0);
    }
    // End helper functions
    
    @Override
    public Object visitPrint(final Print node) {
        java.util.List<expr> values = new java.util.ArrayList<expr>();
        for (int i = 0; i < subs.size(); i++) {
            values.add(makeExpr(subs.get(i)));
        }
        node.setValues(new AstList(values, AstAdapters.exprAdapter));
        return node;
        /*
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
            
            public String toString() {
                String ret = "(Print ";
                for (Generator g : subs) {
                    ret += g.toString() + " ";
                }
                ret += ")";
                return ret;
            }
        };*/
    }
    
    @Override
    public Object visitDelete(final Delete node) {
        java.util.List<expr> args = new java.util.ArrayList<expr>();
        for (int i = 0; i < subs.size(); i++) {
            args.add(makeExpr(subs.get(i)));
        }
        node.setTargets(new AstList(args, AstAdapters.exprAdapter));
        return node;
        /*return new Generator() {
            
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
        };*/
    }
    
    @Override
    public Object visitPass(final Pass node) {
        return node;
        /*return new Generator() {
            
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
        };*/
    }
    
    @Override
    public Object visitBreak(final Break node) {
        return node;
        /*return new Generator() {
            
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
        };*/
    }
    
    @Override
    public Object visitContinue(final Continue node) {
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Continue
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
        };*/
    }
    
    @Override
    public Object visitYield(final Yield node) {
        //expr value = subs.get(0).generateExpr();
        node.setValue(makeExpr(subs.get(0)));
        return node;
        /*return new Generator() {
            
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
        };*/
    }
    
    @Override
    public Object visitReturn(final Return node) {
        //expr value = subs.get(0).generateExpr();
        node.setValue(makeExpr(subs.get(0)));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Return
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
        };*/
    }
    
    @Override
    public Object visitRaise(final Raise node) {
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Raise
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
        };*/
    }
    
    @Override
    public Object visitImport(final Import node) {
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Import
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
        };*/
    }
    
    @Override
    public Object visitImportFrom(final ImportFrom node) {
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for ImportFrom
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
        };*/
    }
    
    @Override
    public Object visitGlobal(final Global node) {
        java.util.List<Name> nameNodes = new java.util.ArrayList<Name>();
        for (int i = 0; i < subs.size(); i++) {
            nameNodes.add((Name)(makeExpr(subs.get(i))));
        }
        node.setNames(new AstList(nameNodes, AstAdapters.identifierAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Global
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
        };*/
    }
    
    @Override
    public Object visitExec(final Exec node) {
        expr body = makeExpr(subs.get(0));
        node.setBody(body);
        expr globals = makeExpr(subs.get(1));
        node.setGlobals(globals);
        expr locals = makeExpr(subs.get(2));
        node.setLocals(locals);
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Exec
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
        };*/
    }
    
    @Override
    public Object visitAssert(final Assert node) {
        expr test = makeExpr(subs.get(0));
        node.setTest(test);
        expr msg = makeExpr(subs.get(1));
        node.setMsg(msg);
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for Assert
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
        };*/
    }
    
    @Override
    public Object visitTryFinally(final TryFinally node) {
        java.util.List<stmt> body = ((Suite)(subs.get(0))).getInternalBody();
        node.setBody(new AstList(body, AstAdapters.stmtAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for TryFinally
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
        };*/
    }
    
    @Override
    public Object visitTryExcept(final TryExcept node) {
        java.util.List<stmt> body = ((Suite)(subs.get(0))).getInternalBody();
        node.setBody(new AstList(body, AstAdapters.stmtAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot return expr for TryExcept
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
        };*/
    }
    
    @Override
    public Object visitWhile(final While node) {
        expr test = makeExpr(subs.get(0));
        java.util.List<stmt> body = ((Suite)subs.get(1)).getInternalBody();
        java.util.List<stmt> orelse = ((Suite)subs.get(2)).getInternalBody();
        node.setTest(test);
        node.setBody(new AstList(body, AstAdapters.stmtAdapter));
        node.setOrelse(new AstList(orelse, AstAdapters.stmtAdapter));
        return node;
        /*return new Generator() {
            
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
        };*/
    }
    
    @Override
    public Object visitCall(final Call node) {
        java.util.List<expr> args = new java.util.ArrayList<expr>();
        for (int i = 0; i < subs.size(); i++) {
            args.add(makeExpr(subs.get(i)));
        }
        node.setArgs(new AstList(args, AstAdapters.exprAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
    @Override
    public Object visitSubscript(final Subscript node) {
        expr value = makeExpr(subs.get(0));
        node.setValue(value);
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    /*
    @Override
    public Object visitIndex(final Index node) {
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
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };
    }
    */
    @Override
    public Object visitTuple(final Tuple node) {
        java.util.List<expr> elts = new java.util.ArrayList<expr>();
        for (int i = 0; i < subs.size(); i++) {
            elts.add(makeExpr(subs.get(i)));
        }
        node.setElts(new AstList(elts, AstAdapters.exprAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
    @Override
    public Object visitList(final List node) {
        java.util.List<expr> elts = new java.util.ArrayList<expr>();
        for (int i = 0; i < subs.size(); i++) {
            expr e = makeExpr(subs.get(i));
            if (e != null) {
                elts.add(e);
            }
        }
        node.setElts(new AstList(elts, AstAdapters.exprAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
    @Override
    public Object visitListComp(final ListComp node) {
        expr elt = makeExpr(subs.get(0));
        node.setElt(elt);
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
    @Override
    public Object visitDict(final Dict node) {
        int keyCount = ((PyInteger)(((Num)(makeExpr(subs.get(0)))).getInternalN())).getValue();
        java.util.List<expr> keys = new java.util.ArrayList<expr>();
        for (int i = 1; i < keyCount+1; i++) {
            keys.add(makeExpr(subs.get(i)));
        }
        int valueCount = ((PyInteger)(((Num)(makeExpr(subs.get(keyCount+1)))).getInternalN())).getValue();
        java.util.List<expr> values = new java.util.ArrayList<expr>();
        for (int i = keyCount+2; i < subs.size(); i++) {
            values.add(makeExpr(subs.get(i)));
        }
        node.setKeys(new AstList(keys, AstAdapters.exprAdapter));
        node.setValues(new AstList(values, AstAdapters.exprAdapter));
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
    @Override
    public Object visitRepr(final Repr node) {
        expr value = makeExpr(subs.get(0));
        node.setValue(value);
        return node;
        /*return new Generator() {
            
            public expr generateExpr() {
                return node;
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                return null;    // Cannot remote other
            }
        };*/
    }
    
}
