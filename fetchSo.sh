#!/bin/bash

mode=${1}

wget https://raw.githubusercontent.com/sshaddicts/opencv/repository/libopencv_java320.so

if [[ $mode == "travis" ]];
then
    mv 3rdparty/libopencv_java320.so /usr/lib/libopencv_java320.so
else
    mkdir -p /usr/lib
    mv libopencv_java320.so /usr/lib/libopencv_java320.so
fi

