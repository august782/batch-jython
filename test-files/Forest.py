class Forest(object) :
    
    def __init__(self, pre = None) :
        self.forest = {}
        if pre :
            for k in pre.keys() :
                if type(pre[k]) == list :
                    self.forest[k] = MultiForest(pre[k])
                else :
                    self.forest[k] = pre[k]
    
    def __iter__(self) :
        #return self.forest
        return self
    
    def __next__(self) :
        return self.forest.__next__()
    
    def subforest(self, k) :
        if k in self.forest :
            return self.forest[k]
        else :
            self.forest[k] = MultiForest()
            return self.forest[k]
    
    def get(self, k) :
        return self.forest[k]
    
    def update(self, k, v) :
        self.forest[k] = v
    
    def contains(self, k) :
        return k in self.forest
    
    def toDict(self) :
        d = {}
        for k in self.forest.keys() :
            if type(self.forest[k]) == MultiForest :
                d[k] = self.forest[k].toList()
            else :
                d[k] = self.forest[k]
        return d
    
    def __str__(self) :
        ret = "{ "
        for k in self.forest.keys() :
            ret += str(k) + " : " + str(self.forest[k]) + ", "
        ret += "}"
        return ret

# List of forests
class MultiForest(object) :
    
    # May prefill with list of dictionaries, each representing a forest
    def __init__(self, pre = None) :
        self.multi = [] 
        if pre :
            for d in pre :  # Every element needs to be a dictionary
                self.multi.append(Forest(d))
        self.currentForest = None
        self.index = 0
    
    def __iter__(self) :
        #return self.multi
        return self
    
    def next(self) :
        if self.index >= len(self.multi) :
            self.index = 0
            raise StopIteration
        else :
            ret = self.multi[self.index]
            self.index += 1
            return ret
        #return self.multi.next()
    
    def get(self, k) :
        return self.currentForest.get(k)
    
    def update(self, k, v) :   # If already existing in forest, means make a new forest, start over in loop
        if not self.currentForest :
            self.currentForest = Forest()
        if self.currentForest.contains(k) :
            self.currentForest = Forest()
            self.multi.append(self.currentForest)
        self.currentForest.update(k, v)
    
    def subforest(self, k) :
        return self.currentForest.subforest(k)

    def toList(self) :
        l = []
        for i in self.multi :
            l.append(i.toDict())
        return l
    
    def __str__(self) :
        ret = "[ "
        for i in self.multi :
            ret += str(i) + ", "
        ret += "]"
        return ret

