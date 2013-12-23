class Var(object) :
    def __init__(self, name) :
        self.name = name
    
    def __str__(self) :
        return str(self.name)

class Data(object) :
    def __init__(self, x) :
        self.x = x
    
    def __str__(self) :
        return str(self.x)

class Fun(object) :
    def __init__(self, var, body) :
        self.var = var
        self.body = body
    
    def __str__(self) :
        return "function(" + str(self.var) + ")" + str(self.body)
    
class Prim(object) :
    def __init__(self, op, args) :
        self.op = op
        self.args = args
        self.ops = {'ADD':'+', 'SUB':'-', 'MUL':'*', 'DIV':'/', 'EQ':'==', 'NOTEQ':'!=', 'LT':'<', 'LTE':'<=', 'GT':'>', 'GTE':'>=', 'AND':'&&', 'OR':'||', 'NOT':'!'}
    
    def __str__(self) :
        if (self.op == "SEQ") :
            return " ; ".join(map(str, self.args))
        else :
            return "(" + str(self.args[0]) + " " + self.ops[self.op] + " " + str(self.args[1]) + ")"

class Prop(object) :
    def __init__(self, base, field) :
        self.base = base
        self.field = field
    
    def __str__(self) :
        return str(self.base) + "." + self.field

class Assign(object) :
    def __init__(self, op, target, source) :
        self.op = op
        self.target = target
        self.source = source
    
    def __str__(self) :
        return str(self.target) + " " + str(self.op) + "=" + str(self.source)

class Let(object) :
    def __init__(self, var, expression, body) :
        self.var = var
        self.expression = expression
        self.body = body
    
    def __str__(self) :
        return "var " + str(self.var) + "=" + str(self.expression) + "; " + str(self.body)

class If(object) :
    def __init__(self, condition, thenExp, elseExp) :
        self.condition = condition
        self.thenExp = thenExp
        self.elseExp = elseExp
    
    def __str__(self) :
        return "if (" + str(self.condition) + ") {" + str(self.thenExp) + "} else {" + str(self.elseExp) + "}"

class Loop(object) :
    def __init__(self, var, collection, body) :
        self.var = var
        self.collection = collection
        self.body = body
    
    def __str__(self) :
        #if (type(self.collection) == Out) : # Assume it is an output
        #    var = self.collection.location
        #    collection = self.collection.expression
        #else :
        #    var = self.var
        #    collection = self.collection
        #return "for (" + str(var) + " in " + str(collection) + ") {" + str(self.body) + "}"
        return "for (" + str(self.var) + " in " + str(self.collection) + ") {" + str(self.body) + "}"

class Call(object) :
    def __init__(self, target, method, args) :
        self.target = target
        self.method = method
        self.args = args
    
    def __str__(self) :
        return str(self.target) + "." + str(self.method) + "(" + ",".join(map(str, self.args)) + ")"

class Out(object) :
    def __init__(self, location, expression) :
        self.location = location
        self.expression = expression
    
    def __str__(self) :
        return "OUTPUT(" + '"' + str(self.location) + '",' + str(self.expression) + ")"

class In(object) :
    def __init__(self, location) :
        self.location = location
    
    def __str__(self) :
        return "INPUT(" + '"' + self.location + '")'

class Skip(object) :
    def __init__(self) :
        pass
    
    def __str__(self) :
        return "skip"

