cd /home/ec2-user/server
sudo /usr/bin/java -war -Dserver.port=80 \
    *.war > /dev/null 2> /dev/null < /dev/null &