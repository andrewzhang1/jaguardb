#!/bin/bash


sed -i "s/.*stop=.*/stop=true/g" app.conf.oracle
cat app.conf.oracle |grep stop

