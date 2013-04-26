#!/bin/bash

for i in {1..100}
do
    ~/Projects/batch-jython/dist/bin/jython test-batch.py >> batch.log
    ~/Projects/batch-jython/dist/bin/jython test-pyro.py >> pyro.log
    ~/Projects/batch-jython/dist/bin/jython test-rpyc.py >> rpyc.log
done

python average.py batch.log >> batch_data
python average.py pyro.log >> pyro_data
python average.py rpyc.log >> rpyc_data

