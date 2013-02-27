import sys

sys.path.append("../rpyc-3.2.3")
import rpyc

from time import clock

conn = rpyc.connect("localhost", 9981)
log = open('rpyc.log', 'w')
times = []
for i in range(100) :
    local = []
    begin = clock()
    for x in conn.root.range(100) :
        local.append(x)
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))
