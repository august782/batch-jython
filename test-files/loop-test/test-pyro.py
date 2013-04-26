import sys

sys.path.insert(0, '../Pyro4-4.17')
import Pyro4

from time import clock

proxy = Pyro4.Proxy("PYRO:service@smarmy-pirate.cs.utexas.edu:9975")

local = []
begin = clock()
for x in range(1000) :
    local.append(proxy.getcwd())
end = clock()
print str(end - begin)

