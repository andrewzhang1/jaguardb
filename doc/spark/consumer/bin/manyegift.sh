#!/bin/bash
tot=$1
if [[ "x$tot" = "x" ]]; then
        tot=7;
fi

echo "$tot concurrent submit_spark.sh ..."
m=5000
((n=1))
while ((n<=tot))
do
        ((o=m+n))
        ./egift_submit_spark.sh $o > "$n".log 2>&1 &
        ((n=n+1))
done
