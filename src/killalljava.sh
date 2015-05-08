#!/bin/bash


# Change this to your netid
netid=dxa132330

#
# Root directory of your project
PROJDIR=$HOME/advanced-operating-system/projects/dynamic-voting
#PROJDIR=$HOME/Documents/FALL-2013-COURSES/Imp_Data_structures/workspace/node-discovery/src

#
# This assumes your config file is named "config.txt"
# and is located in your project directory
#
CONFIG=$PROJDIR/config.txt

#
# Directory your java classes are in
#
BINDIR=$PROJDIR

#
# Your main project class
#
PROG=Fileread

n=0

cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "n"
(
    #read i
    #echo $i
    while read line 
    do
        host=$( echo $line | awk '{ print $3 }' )
        #nodeId=$( echo $line | awk '{ print $2 }' )

        domain=".utdallas.edu"
        machine=$host$domain
		
		echo ssh -l $netid $machine "killall java" &
		ssh -l $netid $machine "killall java" &

        n=$(( n + 1 ))
    done
 
)



