from BatchRemote import BatchRemote

f = BatchRemote()
#print f.Data(1)
mybatch ROOT in f :
    x = 2
    x = ROOT.foo()
    print x
    x + 2
    3
    print "Batch"
    while x :
        print "While"
        break
    ROOT.foo()
    ROOT.y
    for i in [1, 2, 3]  :
        break
    if x :
        print "hello"
    else :
        print "bye"
print "Hello World"
