import socket
import ast

class BatchRemote :
    
    def __init__(self) :
       self.HOST = "localhost"
       self.PORT = 9825
       self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
    def execute(self, expr, forest) :
        try :
            self.sock.connect((self.HOST, self.PORT))
            self.sock.send(expr + "\n")
            received = self.sock.recv(4096)
            print "Received " + str(received)
        finally :
            self.sock.close()
        
        print expr
        received = ast.literal_eval(received)
        print type(received)
        return received # Return the dictionary
    
    def Var(self, name) :
        return name
    
    def Data(self, x) :
        return x
    
    def Prim(self, op, args) :
        # Assume only binary op and SEQ for now
        if (op == "SEQ") :
            return " ; ".join(map(str, args))
        else :
            if op == "ADD" :
                return "(" + str(args[0]) + " " + '+' + " " + str(args[1]) + ")"
            return "(" + str(args[0]) + " " + op + " " + str(args[1]) + ")"
    
    def Prop(self, base, field) :
        return base + "." + field
    
    def Assign(self, op, target, source) :
        return target + " " + op + "= " + source    # Consider case of just regular assign
    
    def Let(self, var, expression, body) :
        return "var " + var + "=" + str(expression) + "; " + body
    
    def If(self, condition, thenExp, elseExp) :
        return "if (" + str(condition) + ") {" + str(thenExp) + "} else {" + str(elseExp) + "}"
    
    def Loop(self, var, collection, body) :
        return "for (" + str(var) + " in " + str(collection) + ") {" + body + "}"
    
    def Call(self, target, method, args) :
        return target + "." + method + "(" + ','.join(args) + ")"
    
    def Out(self, location, expression) :
        return "OUTPUT(" + '"' + location + '",' + str(expression) + ")"
    
    def Skip(self) :
        return "skip"

