start java Server -tcpport 9989 -record 13:25622
timeout 1
start java Server -tcpport 9997 -record 15:10 -connect localhost:9989
timeout 1
start java Server -tcpport 9990 -record 17:2563 -connect localhost:9997 -connect localhost:9989
timeout 1
start java Server -tcpport 9991 -record 12:2562 -connect localhost:9990 -connect localhost:9997 -connect localhost:9989
timeout 1


start java Client -gateway localhost:9991 -operation terminate
 
