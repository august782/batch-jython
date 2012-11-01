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

// Change the batch objects into lines of python code, will then parse again to get AST
public class ConvertFactory extends PartitionFactoryHelper<Generator> {
    
    private static final java.util.Map<Op, operatorType> operators = new java.util.HashMap<Op, operatorType>();
    static {
        operators.put(null, operatorType.UNDEFINED);
        operators.put(Op.ADD, operatorType.Add);
        operators.put(Op.SUB, operatorType.Sub);
        operators.put(Op.MUL, operatorType.Mult);
        operators.put(Op.DIV, operatorType.Div);
        operators.put(Op.MOD, operatorType.Mod);
        //operators.put(operatorType.Pow, Op.);
        //operators.put(operatorType.LShift, "<<");
        //operators.put(operatorType.RShift, ">>");
        //operators.put(operatorType.BitOr, "|");
        //operators.put(operatorType.BitXor, "^");
        //operators.put(operatorType.BitAnd, "&");
        //operators.put(operatorType.FloorDiv, "//");
    }
    
    private String forest;
    private expr service;
    private ConvertFactory self;    // Total hack...
    
    public ConvertFactory(String forest, expr service) {
        super();
        this.forest = forest;
        this.service = service;
        self = this;
    }
    
    // Helper functions
    private mod makeMod(stmt s) {
        // Make mod by wrapping inside suite ??
        java.util.List<stmt> body = new java.util.ArrayList<stmt>();
        body.add(s);
        return  new Suite(new AstList(body, AstAdapters.stmtAdapter));
    }
    
    private expr gen(String method, java.util.List<expr> args) {
        return new Call(new Attribute(service, new PyString(method), AstAdapters.expr_context2py(expr_contextType.Load)), new AstList(args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
    }
    // End helper functions
    
    public Generator Var(final String name) {
        return new Generator() {
            
            public expr generateExpr() {
                return new Name(new PyString(name), AstAdapters.expr_context2py(expr_contextType.Load));    // Set the variable to be load context at first, have others change later ??
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(name)));
                
                return gen("Var", args);
            }
        };
    }
    
    public Generator Data(final Object value) {
        
        return new Generator() {
            
            public expr generateExpr() {
                if (value instanceof java.util.List) {
                    java.util.List<expr> elts = new java.util.ArrayList<expr>();
                    for (Object o : (java.util.List)value) {
                        expr elt = (((PExpr)o).runExtra(self)).generateExpr(); // Some crazy type-casting
                        elts.add(elt);
                    }
                    return new List(new AstList(elts, AstAdapters.exprAdapter), AstAdapters.expr_context2py(expr_contextType.Load));
                }
                else if (value instanceof Integer) {
                    return new Num(new PyInteger(((Integer)value).intValue()));
                }
                else if (value instanceof String) {
                    return new Str(new PyString((String)value));
                }
                else {
                    return null;    // I hope to never each here..
                }
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(generateExpr());
                
                return gen("Data", args);
            }
        };
    }
    
    public Generator Fun(String var, Generator body) {return null;}
    
    public Generator Prim(final Op op, final java.util.List<Generator> args) {
        PythonTree ret;
        return new Generator() {
            
            public expr generateExpr() {
                if (op == Op.SEQ) {
                    return null;    // If it's sequence, cannot return expr
                }
                // Basic expr operations, involving lefts and rights
                expr left, right;
                left = args.get(0).generateExpr();
                for (int i = 1; i < args.size(); i++) {
                    right = args.get(i).generateExpr();
                    left = new BinOp(left, AstAdapters.operator2py(operators.get(op)), right);
                }
                
                return left;
            }
            
            public mod generateMod() {
                if (op == Op.SEQ) {
                    // For a sequence of arguments, return a suite
                    java.util.List<stmt> body = new java.util.ArrayList<stmt>();
                    for (Generator g : args) {
                        // Just in case adding a Suite, break it apart and combine
                        body.addAll(((Suite)g.generateMod()).getInternalBody());
                    }
                    return (new Suite(new AstList(body, AstAdapters.stmtAdapter)));
                }
                else {
                    return makeMod(generateStmt()); // Was just basic case, use makeMod()
                }
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> prim_args = new java.util.ArrayList<expr>();
                prim_args.add(new Str(new PyString(op.toString())));
                java.util.List<expr> args_args = new java.util.ArrayList<expr>();
                for (Generator g : args) {
                    args_args.add(g.generateRemote());
                }
                prim_args.add(new List(new AstList(args_args, AstAdapters.exprAdapter), AstAdapters.expr_context2py(expr_contextType.Load)));
                
                return gen("Prim", prim_args);
            }
        };
    }
    
    public Generator Prop(final Generator base, final String field) {
        return new Generator() {
        
            public expr generateExpr() {
                return new Attribute(base.generateExpr(), new PyString(field), AstAdapters.expr_context2py(expr_contextType.Load)); // Set context as load for now
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(base.generateRemote());
                args.add(new Str(new PyString(field)));
                
                return gen("Prop", args);
            }
        };
    }
    
    public Generator Assign(final Op op, final Generator target, final Generator source) {
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // No expr for AugAssign node
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new AugAssign(target.generateExpr(), AstAdapters.operator2py(operators.get(op)), source.generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(op.toString())));
                args.add(target.generateRemote());
                args.add(source.generateRemote());
                
                return gen("Assign", args);
            }
        };
    }
    
    public Generator Let(final String var, final Generator expression, final Generator body) {
        return new Generator() {
              
            public expr generateExpr() {
                return null;    // No case for returning expr
            }

            public mod generateMod() {
                java.util.List<expr> targets = new java.util.ArrayList<expr>();
                targets.add(new Name(new PyString(var), AstAdapters.expr_context2py(expr_contextType.Store)));  // Assign puts variable in store context
                expr value = expression.generateExpr();
                Assign a = new Assign(new AstList(targets, AstAdapters.exprAdapter), value);
                java.util.List<stmt> statements = new java.util.ArrayList<stmt>();
                statements.add(a);
                statements.addAll(((Suite)body.generateMod()).getInternalBody()); // generateMod() should return a mod

                return new Suite(new AstList(statements, AstAdapters.stmtAdapter));
            }

            public stmt generateStmt() {
                return null;    // No case for returning stmt
            }

            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(var)));
                args.add(expression.generateRemote());
                args.add(body.generateRemote());

                return gen("Let", args);
            }
        };
    }
    
    public Generator If(final Generator condition, final Generator thenExp, final Generator elseExp) {
        return new Generator() {
            
            public expr generateExpr() {
                return new IfExp(condition.generateExpr(), thenExp.generateExpr(), elseExp.generateExpr());
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new If(condition.generateExpr(), new AstList(((Suite)thenExp.generateMod()).getInternalBody()), new AstList(((Suite)elseExp.generateMod()).getInternalBody()));
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(condition.generateRemote());
                args.add(thenExp.generateRemote());
                args.add(elseExp.generateRemote());
                
                return gen("If", args);
            }
        };
    }
    
    public Generator Loop(final String var, final Generator collection, final Generator body) {
        return new Generator() {
            
            public expr generateExpr() {
                return null;    // Cannot generate expr from a loop
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                expr target = new Name(new PyString(var), AstAdapters.expr_context2py(expr_contextType.Store));
                // Save forest name for use in case of inputs
                String save = new String(forest);
                forest = new String(var);
                java.util.List<stmt> loop_body = new java.util.ArrayList<stmt>();
                loop_body.addAll(((Suite)body.generateMod()).getInternalBody());
                forest = new String(save);
                return new For(target, collection.generateExpr(), new AstList(loop_body, AstAdapters.stmtAdapter), Py.None);
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(var)));
                args.add(collection.generateRemote());
                args.add(body.generateRemote());
                
                return gen("Loop", args);
            }
        };
    }
    
    public Generator Call(final Generator target, final String method, final java.util.List<Generator> args) {
        return new Generator() {
            
            public expr generateExpr() {
                Attribute func = new Attribute(target.generateExpr(), new PyString(method), AstAdapters.expr_context2py(expr_contextType.Load));
                java.util.List<expr> expr_args = new java.util.ArrayList<expr>();
                for (Generator g : args) {
                    // The PythonTree only holds Expr type
                    expr_args.add(g.generateExpr());
                }
                return new Call(func, new AstList(expr_args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None); // No such thing as keywords, starargs, or kwargs in batch language
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> call_args = new java.util.ArrayList<expr>();
                call_args.add(target.generateRemote());
                call_args.add(new Str(new PyString(method)));
                java.util.List<expr> args_args = new java.util.ArrayList<expr>();
                for (Generator g : args) {
                    args_args.add(g.generateRemote());
                }
                call_args.add(new List(new AstList(args_args, AstAdapters.exprAdapter), AstAdapters.expr_context2py(expr_contextType.Load)));
                
                return gen("Call", call_args);
            }
        };
    }
    
    public Generator In(final String location) {
        return new Generator() {
            
            public expr generateExpr() {
                // For In, just access the given dictionary name
                Attribute dict = new Attribute(new Name(new PyString(forest), AstAdapters.expr_context2py(expr_contextType.Load)), new PyString("get"), AstAdapters.expr_context2py(expr_contextType.Load));
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(location)));
                return new Call(dict, new AstList(args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(location)));
                
                return gen("In", args);
            }
        };
    }
    
    public Generator Out(final String location, final Generator expression) {
        return new Generator() {
              
            public expr generateExpr() {
                // For Out, store into given dictionary name
                Attribute dict = new Attribute(new Name(new PyString(forest), AstAdapters.expr_context2py(expr_contextType.Load)), new PyString("update"), AstAdapters.expr_context2py(expr_contextType.Load));
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                java.util.List<expr> keys = new java.util.ArrayList<expr>();
                keys.add(new Str(new PyString(location)));
                java.util.List<expr> values = new java.util.ArrayList<expr>();
                values.add(expression.generateExpr());
                Dict temp_dict = new Dict(new AstList(keys, AstAdapters.exprAdapter), new AstList(values, AstAdapters.exprAdapter));
                args.add(temp_dict);
                return new Call(dict, new AstList(args, AstAdapters.exprAdapter), Py.None, Py.None, Py.None);
            }
            
            public mod generateMod() {
                return makeMod(generateStmt());
            }
            
            public stmt generateStmt() {
                return new Expr(generateExpr());
            }
            
            public expr generateRemote() {
                java.util.List<expr> args = new java.util.ArrayList<expr>();
                args.add(new Str(new PyString(location)));
                args.add(expression.generateRemote());
                return gen("Out", args);
            }
        };
    }
    
    public String RootName() {return null;}
    
    public Generator Root() {return null;}
    
    public Generator Assign(Generator target, Generator source) {
        return Assign(null, target, source);
    }
    
    public Generator Prim(Op op, Generator... args) {
        return Prim(op, args);
    }
    
    public Generator Call(Generator target, String method, Generator... args) {
        return Call(target, method, args);
    }
    
    public Generator Skip() {return null;}
    
	public Generator Other(Object external, Generator... subs) {
        return Other(external, subs);
    }
    
	public Generator Other(Object external, java.util.List<Generator> subs) {
        // Assume a PythonTree node was placed as the external
        PythonTree node = (PythonTree)external;
        ConvertOther visitor = new ConvertOther(subs, this);
        Generator ret = null;
        try {
            ret = (Generator)visitor.visit(node);
        } catch (Exception e) {
            System.err.println("Error that should not have happened has occured");
            e.printStackTrace();
            System.exit(1); // Technically should never get here?
        }
        return ret;
    }
    
	public Generator DynamicCall(Generator target, String method, java.util.List<Generator> args) {return null;}
    
	public Generator Mobile(String type, Object obj, Generator exp) {return null;}

    @Override
    public Generator Mobile(String type, Generator exp) {
        throw new Error("NOT DEFINED");
    }

    @Override
    public Generator setExtra(Generator exp, Object extra) {
        //System.out.println("MISSING CASE??");
        return exp;
    }

}

