# **Distributed_Database**

## **The goal of this project**
### In this project u can create several nodes that stores some data: (key):(value). You can define connections between each node. After the network of nodes is created u can connect to any of them and perform some action!
## **How to run the project**
### 1.The easiest way is to run the script that's in the src folder. 
### 2.To create a single node, in your command line write: start java DatabaseNode -tcpport (Port on witch u want to run the server) -record (key):(value) -connect (Address of other server):(Other servers port)
## **Classes**
* ### DatabaseClient - responsible for connecting to server
* ### DatabaseNode - listens for clients that want to connect to server
* ### DatabaseNodeConfig - configures server data, such as: it's port, all available servers, keys and values
* ### ServerThread - executes all available methods and can call other servers
* ### Functions - Class that contains functions that are used in more than one class

## **Available actions**
* ### get-value (key) -- returns value that stored under the given key
* ### set-value (key:value) -- sets value of given key
* ### get-min -- returns the smallest value stored in all nodes
* ### get-max -- returns the biggest value stored in all nodes
* ### find-key (key) -- returns the address of server that stores the given key
* ### new-record (key:value) -- adds value to the node that client is connected to
* ### terminate -- destroys the node that client is connected to
