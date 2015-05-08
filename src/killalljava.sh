#!/bin/bash


# Change this to your netid
netid=dxa132330

#
# Root directory of your project
PROJDIR=$HOME/advanced-operating-system/projects/dynamic-voting

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
PROG=DynamicVoting

x=0

head -n 24 $CONFIG | tail -n $((24-15+1)) | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    while read line 
    do
        host=$( echo $line | awk '{ print $2 }' )
		host_name="dc"$host
		#echo $netid@$host.utdallas.edu java $BINDIR/$PROG $x &		
        #ssh $netid@$host.utdallas.edu java $BINDIR/$PROG $x &

		
		echo ssh -l $netid $host_name.utdallas.edu "killall java" &
		ssh -l $netid $host_name.utdallas.edu "killall java" &
		
        x=$(( x + 1 ))
    done
   
)


cat $CONFIG | sed -e "s/#.*//" | sed -e "/^\s*$/d" | grep "n" |
(
#read i
#echo $i
while read line
do
host=$( echo $line | awk '{ print $3 }' )
nodeId=$( echo $line | awk '{ print $2 }' )

domain=".utdallas.edu"
machine=$host$domain
val=$netid@$host$domain
echo $val
echo $nodeId

#ssh $val java $PROJDIR NodeDiscovery $nodeId &

ssh -q -o StrictHostKeyChecking=no -l "$netid" "$machine" "cd $PROJDIR;java DynamicVoting $nodeId" &

#cmd="ssh $val java $PROJDIR NodeDiscovery $nodeId &"
#echo $cmd
#cmd

n=$(( n + 1 ))
done

)



