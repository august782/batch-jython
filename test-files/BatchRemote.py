import socket
from com.xhaus.jyson import JysonCodec as json

from Forest import Forest

class BatchRemote(object) :
    
    def __init__(self) :
       self.HOST = 'localhost'
       self.PORT = 9825
       self.ops = {'ADD':'+', 'SUB':'-', 'MUL':'*', 'DIV':'/', 'EQ':'==', 'NOTEQ':'!=', 'LT':'<', 'LTE':'<=', 'GT':'>', 'GTE':'>=', 'AND':'&&', 'OR':'||', 'NOT':'!'}
    
    def makeForest(self) :
        return Forest()
    
    def execute(self, expr, forest) :
        try :
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.connect((self.HOST, self.PORT))
            #self.sock.sendall(expr + "\n")
            #self.sock.shutdown(socket.SHUT_WR)
            #received = self.sock.recv(4096)
            f = sock.makefile()
            #print forest.toDict()
            f.write(str(expr) + "\n")
            f.write(json.dumps(forest.toDict()) + "\n")
            #print str(expr)
            f.flush()
            header = f.readline()
            received = f.readline()
            #print "Header " + str(header)
            #print "Received " + str(received)
            f.close()
        finally :
            sock.close()
        
        # Possible no dictionary received, so hold off on loading
        if received :
            received = json.loads(received)
        else :
            received = {}
        new_forest = Forest(received)
        #print str(new_forest)
        return new_forest     # Return the forest
    
    def Var(self, name) :
        return name
    
    def Data(self, x) :
        return x
    
    def Fun(self, var, body) :
        return "function(" + var + ")" + body
    
    def Prim(self, op, args) :
        # Assume only binary op and SEQ for now
        if (op == "SEQ") :
            return " ; ".join(map(str, args))
        else :
            return "(" + str(args[0]) + " " + self.ops[op] + " " + str(args[1]) + ")"
    
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
        return target + "." + method + "(" + ','.join(map(str, args)) + ")"
    
    def Out(self, location, expression) :
        return "OUTPUT(" + '"' + location + '",' + str(expression) + ")"
    
    def In(self, location) :
        return "INPUT(" + '"' + location + '")'
    
    def Skip(self) :
        return "skip"

