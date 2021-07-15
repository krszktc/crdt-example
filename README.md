# Info

CRDT graph implementation base on LWW-Element-Set and G-Set.

# Instalation

* run "mvn clean install" in application folder to build and run tests
* run "mvn test" to execute only tests on already builder application

# Functionality

* add/remove vertex
* add/remove edge
* check is edge/vertex exist
* get single edge
* get all edges
* find path between two vertices
* replicate graph

# Usage

All vertices have it's names and values. To create graph init point and value need to be specified.
To add next vertex, "parent" need to be pointed. Adding new vertex make the value replicated from parent.
To change graph status, any vertex's value need to be change. The change is replicate to all elements.

Graph can return self replica, which is the newest state. To make more replicas synchronized, each of them
need to get the same set of messages. In the demo it can be achieved by execution the same action 
on all graph collection elements. In real world similar behavior is usually done by message system.



