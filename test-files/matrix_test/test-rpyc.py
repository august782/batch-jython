import sys

sys.path.insert(0, '../rpyc-3.2.3')
import rpyc

from time import clock

conn = rpyc.connect("localhost", 9981)
log = open('rpyc.log', 'w')
times = []
for i in range(100) :
    local = []
    begin = clock()
    conn.root.matrix()
    conn.root.mult()
    for r in conn.root.rows() :
        for c in conn.root.cols() :
            local.append(conn.root.item(r*3+c))
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))

