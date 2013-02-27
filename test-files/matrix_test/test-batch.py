import sys
sys.path.append("../")

from BatchRemote import BatchRemote

from time import clock

f = BatchRemote()
log = open('batch.log', 'w')
times = []
for i in range(100) :
    local = []
    begin = clock()
    mybatch ROOT in f :
        ROOT.matrix()
        ROOT.mult()
        for r in ROOT.rows() :
            for c in ROOT.cols() :
                local.append(ROOT.item(r*3+c))
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))

