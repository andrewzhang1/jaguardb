#!/bin/bash

if [[ ! -d "apache-ant-1.10.1" ]]; then
	tar zxf apache-ant-1.10.1-bin.tar.gz
fi

if readlink -qs ant > /dev/null
then
	/bin/true
else
	ln -s apache-ant-1.10.1 ant
fi
