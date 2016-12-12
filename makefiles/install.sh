#!/bin/sh

if [ $# -lt 3 ]
then
    exit 0
fi

mode=$1
shift
destdir=$1
shift

for pathname in "$@"
do

    filename=`basename $pathname`
    echo "%%% Installing "$pathname" in directory "$destdir
    if [ -f $destdir/$filename ]
    then
	ln $destdir/$filename $destdir/$filename'.old'$$ > /dev/null
	rm -f $destdir/$filename > /dev/null
    fi
    cp $pathname $destdir
    if [ $? -ne 0 ]
    then
	exit 1
    fi
    chmod $mode $destdir/$filename
    rm -f $destdir/$filename'.old'$$
done
exit 0
