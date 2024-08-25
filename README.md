# echoecho

Echo TCP server which can asynchronously accepts request from multiple
clients. This is a small POC for creating a TCP server that can accept
connection from multiple server at same time.

The default port exposed is - 127.0.0.1:10000

## Getting Started

To start the program, first clone this repository.
Start server. A echo can either be a `server` or a `client`.
You should only have one `server` running as configuring port is not
allowed as of now. Create a single server and can have several
connection to that server. Upon starting the echo, you will be
presented with following options

``` shell
$ clj -M -m echoecho.echo
  For being a server, press s
  For being a client, press c
  What are you? :
```

Press S to create a server. By default host is localhost and port is
10000

If you have chosen to be the client, you can type your username and your
input will be echoed. You can connect multiple client as the server
process request asynchronously.


``` shell
$ clj -M -m echoecho.echo
  For being a server, press s
  For being a client, press c
  What are you? : c
  What is your name : username
  username  : this
  Server Response :  this
  username  : that
  Server Response :  that
  username  : with
  Server Response :  with
  username  : more time required
  Server Response :  more time required
  username  :
```
