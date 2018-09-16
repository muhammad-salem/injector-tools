# injector-tools (0.3.3-dev)


 - test server security
 - good understand to different payload format
 - create local proxy with different payload at a time
 - connect to ssh server (PortForwarder)
 - provide http and socks5 proxy for connected ssh server
 - connect using openvpn
 - polipo server
 - redirect all trafic to redsocks in linux platform using iptables
 - show current speed and used traffic
 - event api
 - log freamwork
 - config formate


# Start app
java -jar injector-tools-0.3.3-dev-jar-with-dependencies.jar filename.json

 - -h	show help message
 - -v	show app version
 - -t 	create json templete configuration file
 - filename	load config file
 - filename:	the name of the file to be load
 - if no config file loaded will use default config (cache dir)
 - -h:help,	-v:version,	-t:temp
 
# Fix ISSUS:
	- fix ANSI escape code on windows platform. 
 
