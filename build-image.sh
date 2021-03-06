#!/bin/bash

#clean maven
mvn clean package

#Run maven
mvn install 

# Exit if the build fail
if [[ "$?" -ne 0 ]] ; then
    echo 'could not perform tests' ; exit $rc
fi

docker build -t amt/help2000 .
