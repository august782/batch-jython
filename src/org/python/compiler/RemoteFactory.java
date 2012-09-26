// Copyright (c) Corporation for National Research Initiatives
package org.python.compiler;

import org.python.antlr.adapter.AstAdapters;
import org.python.core.AstList;

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

public class RemoteFactory extends PartitionFactoryHelper<PythonTree> {
    
    private expr service;
    
    public RemoteFactory(expr service) {
        this.service = service;
    }
    
    public PythonTree Var(String name) {
        java.util.List<expr> args = new java.util.ArrayList<expr>();
        args.add(new Str(new PyString(name)));
        return new Call(new Attribute(service, new PyString("Var"), AstAdapters.expr_context2py(expr_contextType.Load)), new AstList(args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
    }
    
    public PythonTree Data(Object value) {
        java.util.List<expr> args = new java.util.ArrayList<expr>();
        if (value instanceof Integer) {
            args.add(new Num(new PyInteger((Integer)(value))));
        }
        return new Call(new Attribute(service, new PyString("Data"), AstAdapters.expr_context2py(expr_contextType.Load)), new AstList(args, AstAdatpers.exprAdatper), Py.None, Py.None, Py.None);
    }
    
    public PythonTree Fun(String var, PythonTree body) {return null;}
    
    public PythonTree Prim(Op op, java.util.List<PythonTree> args) {
        java.util.List<expr> prim_args = new java.util.ArrayList<expr>();
        prim_args.add(new Str(new PyString(op.toString())));
        java.util.List<expr> args_args = new java.util.ArrayList<expr>();
        for (PythonTree t : args) {
            args_args.add((expr)t);
        }
        prim_args.add(new List(new AstList(args_args, AstAdapters.exprAdapter), AstAdapters.expr_context2py(expr_contextType.Load)));
        
        return new Call(new Attribute(service, new PyString("Prim"), AstAdapters.expr_context2py(expr_contextType.Load)), new AstList(prim_args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
        
    }
    
    public PythonTree Prop(PythonTree base, String field) {return null;}
    
    public PythonTree Assign(Op op, PythonTree target, PythonTree srouce) {return null;}
    
    public PythonTree Let(String var, PythonTree expression, PythonTree body) {return null;}
    
    public PythonTree If(PythonTree condition, PythonTree thenExp, PythonTree elseExp) {return null;}
    
    public PythonTree Loop(String var, PythonTree collection, PythonTree bocy) {return null;}
    
    public PythonTree Call(PythonTree target, String method, java.util.List<PythonTree> args) {
        java.util.List<expr> call_args = new java.util.ArrayList<expr>();
        call_args.add((expr)target);
        call_args.add(new Str(new PyString(method)));
        java.util.List<expr> args_args = new java.util.ArrayList<expr>();
        for (PythonTree p : args) {
            args_args.add((expr)p);
        }
        call_args.add(new List(new AstList(args_args, AstAdapters.exprAdapter), AstAdapters.expr_context2py(expr_contextType.Load)));
        return new Call(new Attribute(service, new PyString("Call"), AstAdapters.expr_context2py(expr_contextType.Load)), new AstList(call_args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
    }
    
    public PythonTree In(String location) {return null;}
    
    public PythonTree Out(String location, PythonTree expression) {return null;}
    
    public String RootName() {return null;}
    
    public PythonTree Root() {return null;}
    
    public PythonTree Assign(PythonTree target, PythonTree source) {
        return Assign(null, target, source);
    }
    
    public PythonTree Prim(Op op, PythonTree... args) {
        return Prim(op, args);
    }
    
    public PythonTree Call(PythonTree target, String method, PythonTree... args) {
        return Call(target, method, args);
    }
    
    public PythonTree Skip() {return null;}
    
	public PythonTree Other(Object external, PythonTree... subs) {
        return Other(external, subs);
    }
    
	public PythonTree Other(Object external, java.util.List<PythonTree> subs) {return null;}
    
	public PythonTree DynamicCall(PythonTree target, String method, java.util.List<PythonTree> args) {return null;}
    
	public PythonTree Mobile(String type, Object obj, PythonTree exp) {return null;}

    @Override
    public PythonTree Mobile(String type, PythonTree exp) {
        throw new Error("NOT DEFINED");
    }

    @Override
    public PythonTree setExtra(PythonTree exp, Object extra) {
        //System.out.println("MISSING CASE??");
        return exp;
    }
    
}
