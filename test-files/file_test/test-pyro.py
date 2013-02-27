import sys

sys.path.insert(0, '../Pyro4-4.17')
import Pyro4

from time import clock

log = open('pyro.log', 'w')
times = []
proxy = Pyro4.Proxy("PYRO:example.service@localhost:54642")
for i in range(100) :
    local = []
    begin = clock()
    for files in proxy.getFiles(proxy.getcwd()) :
        for file in files :
            local.append(file)
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))

