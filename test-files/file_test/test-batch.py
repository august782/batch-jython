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
        for files in ROOT.getFiles(ROOT.getcwd()) :
            for file in files :
                local.append(file)
    end = clock()
    times.append(end - begin)
    log.write(str(end - begin) + "\n")
log.write("Average: " + str(reduce(lambda x, y: x+y, times)/len(times)))

