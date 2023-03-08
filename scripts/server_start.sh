#!/usr/bin/env bash
cd /home/ec2-user/server
mvn clean install
cd target
sudo java -jar -Dserver.port=8080 \
    *.war > /dev/null 2> /dev/null < /dev/null &