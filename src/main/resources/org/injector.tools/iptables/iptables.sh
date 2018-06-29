#!/bin/bash

# {"sudo",  "-S", "./redsocks.sh" }

#dir start type host port auth user pass
# $1 {/temp/}
# $2 {start, stop}
# $4 socks5
# $5 127.0.0.1
# $6 1080


DIR=$1
type=$3
host=$4
port=$5
auth=$6
user=$7
pass=$8

#PATH=$DIR:$PATH
BIN=$DIR/redsocks
CONF=$DIR/redsocks.conf
WATCH=$DIR/watch.lock

#PID=$DIR/redsocks.pid


startIpTables () {
#pdnsd -c $CONF_PDNSD

 $BIN -c $CONF
 #redsocks -c $CONF
 # Create new chain
 iptables -t nat -N REDSOCKS
 # Exclude local and reserved addresses
 iptables -t nat -A REDSOCKS -d 0.0.0.0/8 -j RETURN
 iptables -t nat -A REDSOCKS -d 10.0.0.0/8 -j RETURN
 iptables -t nat -A REDSOCKS -d 127.0.0.0/8 -j RETURN
 iptables -t nat -A REDSOCKS -d 169.254.0.0/16 -j RETURN
 iptables -t nat -A REDSOCKS -d 172.16.0.0/12 -j RETURN
 iptables -t nat -A REDSOCKS -d 192.168.0.0/16 -j RETURN
 iptables -t nat -A REDSOCKS -d 224.0.0.0/4 -j RETURN
 iptables -t nat -A REDSOCKS -d 240.0.0.0/4 -j RETURN

 iptables -t nat -A REDSOCKS -p tcp -j REDIRECT --to-ports 4123

 # Redirect all HTTP and HTTPS outgoing packets through Redsocks
 iptables -t nat -A OUTPUT -p tcp --dport 443 -j REDSOCKS
 iptables -t nat -A OUTPUT -p tcp --dport 80 -j REDSOCKS

 iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDSOCKS
 iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDSOCKS
 iptables -t nat -A PREROUTING -p tcp --dport 1080 -j REDSOCKS

 #iptables -t nat -A OUTPUT -p udp --dport 53 -j REDIRECT --to-ports 8533

}

stopIpTables () {
  # Delete new chain

  iptables -t nat -F PREROUTING
  iptables -t nat -F INPUT
  iptables -t nat -F OUTPUT
  iptables -t nat -F POSTROUTING
  iptables -t nat -F REDSOCKS
  iptables -t nat -X REDSOCKS


  killall -9 redsocks
  #killall -9 pdnsd
  #kill -9 `cat $PID`
  #rm $PID
  #rm $CONF
}

case $2 in
 start)

echo "
base {
 log_debug = on;
 log_info = on;
 log = \"$DIR/redsocks.log\";
 daemon = on;
 redirector = iptables;
}
" >$CONF
proxy_port=4123

 case $type in
  http)
  proxy_port=4124
 case $auth in
  true)
  echo "
redsocks {
 local_ip = 127.0.0.1;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = http-relay;
 login = \"$user\";
 password = \"$pass\";
}
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4124;
 ip = $host;
 port = $port;
 type = http-connect;
 login = \"$user\";
 password = \"$pass\";
}
" >>$CONF
   ;;
   false)
   echo "
redsocks {
 local_ip = 127.0.0.1;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = http-relay;
}
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4124;
 ip = $host;
 port = $port;
 type = http-connect;
}
 " >>$CONF
   ;;
 esac
   ;;
  socks5)
   case $auth in
  true)
    echo "
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = socks5;
 login = \"$user\";
 password = \"$pass\";
 }
 " >>$CONF
   ;;
 false)
  echo "
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = socks5;
 }
 " >>$CONF
   ;;
 esac
 ;;
   socks4)
   case $auth in
  true)
    echo "
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = socks4;
 login = \"$user\";
 password = \"$pass\";
 }
 " >>$CONF
   ;;
 false)
  echo "
redsocks {
 local_ip = 0.0.0.0;
 local_port = 4123;
 ip = $host;
 port = $port;
 type = socks4;
 }
 " >>$CONF
   ;;
 esac
 ;;
 esac

 startIpTables
 ;;
stop)
  stopIpTables
esac


### Set initial time of file
LTIME=`stat -c %Z $WATCH`

echo $WATCH
echo $LTIME
while true
do
   ATIME=`stat -c %Z  $WATCH`
   echo $ATIME
   if [[ "$ATIME" != "$LTIME" ]]
   then
       echo "RUN COMMNAD"
       LTIME=$ATIME
       stopIpTables
       exit
   fi
   sleep 5
done
