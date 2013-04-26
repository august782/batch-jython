package org.python.compiler;

import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Suite;
import org.python.antlr.ast.expr_contextType;
import org.python.antlr.base.expr;
import org.python.antlr.base.stmt;
import org.python.core.PyString;

import org.python.antlr.ast.Batch;
import batch.partition.*;

import org.python.antlr.adapter.AstAdapters;
import org.python.core.AstList;

import org.python.antlr.ast.arguments;

import java.util.HashMap;

public class BatchFunction {
    
    private static HashMap<String, FunctionDef> funcs = new HashMap<String, FunctionDef>();
    
    public static void makeFunction(FunctionDef node) throws Exception {
        // In this case, assume all arguments are remote arguments for simplicity's sake...
        java.util.List<stmt> oldbody = new java.util.ArrayList<stmt>();
        arguments args = node.getInternalArgs();
        Environment env = new Environment(CodeModel.factory);
        java.util.List<String> locals = new java.util.ArrayList<String>();
        java.util.List<stmt> body = new java.util.ArrayList<stmt>(node.getInternalBody());
        for (expr a : args.getInternalArgs()) {
            Name n = (Name)a;
            env = env.extend(n.getInternalId(), null, Place.REMOTE);    // Add the remote variable to the environment
            locals.add(n.getInternalId());
        }
        ConvertVisitor visitor = new ConvertVisitor(locals, new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Load)), new Name(new PyString("forest"), AstAdapters.expr_context2py(expr_contextType.Load)));
        PExpr seq = (PExpr)(visitor.visitAll(body));
        History h = seq.partition(Place.MOBILE, env);
        PExpr first_local = null;
        PExpr remote_expr = null;
        PExpr second_local = null;
        if (h.length() == 1) {
            if (h.get(0).place().toString().equals("LOCAL")) {
                first_local = h.get(0).action();
            }
            else if (h.get(0).place().toString().equals("REMOTE")) {
                remote_expr = h.get(0).action();
            }
        }
        else if (h.length() == 2) {
            if (h.get(0).place().toString().equals("LOCAL")) {
                first_local = h.get(0).action();
                remote_expr = h.get(1).action();
            }
            else if (h.get(0).place().toString().equals("REMOTE")) {
                remote_expr = h.get(0).action();
                second_local = h.get(1).action();
            }
        }
        else if (h.length() == 3) {
            first_local = h.get(0).action();
            remote_expr = h.get(1).action();
            second_local = h.get(2).action();
        }
        // First create the function that deals with pre-local and remote
        java.util.List<stmt> pre_remote_body = new java.util.ArrayList<stmt>();
        java.util.List<expr> arglist = new java.util.ArrayList<expr>();
        arglist.addAll(new java.util.ArrayList<expr>(args.getInternalArgs()));  // Add original arguments to the parameter list
        arglist.add(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Param))); // Pass in service
        arglist.add(new Name(new PyString("forest"), AstAdapters.expr_context2py(expr_contextType.Param)));  // Pass in forest
        if (first_local != null) {
            pre_remote_body.addAll(((Suite)first_local.runExtra(new ConvertFactory(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Load)), "forest")).generateMod()).getInternalBody());
        }
        if (remote_expr != null) {
            String remote_name = ((Name)(arglist.get(0))).getInternalId();
            PExpr let = ConvertVisitor.f.Let(remote_name, (PExpr)(new ConvertVisitor.visit(arglist.get(0))), remote_expr);
            //pre_remote_body.add(new Return(remote_expr.runExtra(new ConvertFactory(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Load)), "forest")).generateRemote()));
            pre_remote_body.add(new Return(let.runExtra(new ConvertFactory(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Load)), "forest")).generateRemote()));
        }
        arguments new_args = new arguments(new AstList(arglist, AstAdapters.exprAdapter), null, null, new AstList(new java.util.ArrayList<expr>(), AstAdapters.exprAdapter));
        FunctionDef pre_remote = new FunctionDef(new PyString("batch_" + node.getInternalName()), new_args, new AstList(pre_remote_body, AstAdapters.stmtAdapter) , new AstList(new java.util.ArrayList<expr>(), AstAdapters.exprAdapter));
        
        funcs.put(pre_remote.getInternalName(), pre_remote);    // Map the function to be used later in code compiler
        
        if (second_local != null) {
            java.util.List<expr> post_arglist = new java.util.ArrayList<expr>();
            post_arglist.addAll(new java.util.ArrayList<expr>(args.getInternalArgs()));
            post_arglist.add(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Param)));
            post_arglist.add(new Name(new PyString("forest"), AstAdapters.expr_context2py(expr_contextType.Param)));
            arguments post_args = new arguments(new AstList(post_arglist, AstAdapters.exprAdapter), null, null, new AstList(new java.util.ArrayList<expr>(), AstAdapters.exprAdapter));
            java.util.List<stmt> post_body = new java.util.ArrayList<stmt>(((Suite)second_local.runExtra(new ConvertFactory(new Name(new PyString("service"), AstAdapters.expr_context2py(expr_contextType.Load)), "forest")).generateMod()).getInternalBody());
            FunctionDef post = new FunctionDef(new PyString("post_" + node.getInternalName()), post_args, new AstList(post_body, AstAdapters.stmtAdapter), new AstList(new java.util.ArrayList<expr>(), AstAdapters.exprAdapter));
            
            funcs.put(post.getInternalName(), post);
        }
    }
    
    // A way to return any newly created batch function for use in scopes compiler and code compiler
    public static FunctionDef getFunction(String name) {
        if (funcs.containsKey(name)) {
            return funcs.get(name);
        }
        else {
            return null;
        }
    }
}
