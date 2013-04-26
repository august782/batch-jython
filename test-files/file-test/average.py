import sys

file_name = sys.argv[1]
f = open(file_name)

lines = f.readlines()

#print "Average is : " + str((reduce(lambda x,y : x+y, map(float, lines))) / len(lines))
print str((reduce(lambda x,y : x+y, map(float, lines))) / len(lines))

