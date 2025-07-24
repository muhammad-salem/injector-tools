# injector-tools (0.5.0-dev)


 - Test server security
 - Good understand to different Payload format
 - Create local proxy with different Payload at a time
 - SNI Host Injection
 - Connect to SSH servers with port forwarder, and provide HTTP and Socks5 proxies
 - provide http and socks5 proxy for connected ssh server
 - show current speed and used traffic
 - Event Listener API
 - Logging Framework
 - Read config from json file. 


# Start app
java -jar injector-tools-0.5.0-dev-jar-with-dependencies.jar filename.json

 - -h	show help message
 - -v	show app version
 - -t 	create json template configuration file
 - filename	load config file
 - filename:	the name of the file to be load
 - if no config file loaded will use default config (cache dir)
 - -h:help,	-v:version,	-t:temp
 
# Fix ISSUS:
	- fix ANSI escape code on windows platform. 
 
