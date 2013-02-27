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
        for x in ROOT.range(100) :
            local.append(x)
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))


