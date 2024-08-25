# echoecho

As a starting step, this will act as an echo server that accepts just
one client.

The default port exposed is - 127.0.0.1:7007

## Getting Started

To start the program, first clone this repository.
y
Start chatroom. A chatroom can either be a `server` or a `client`.
You should only have one `server` running as configuring port is not
allowed as of now. So you need to create a single server and can have
several connection to that server. Upon starting the chatroom, you will
be presented with following options

``` shell
$ clj -M -m echoecho.chatroom
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
$ clj -M -m echoecho.chatroom
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

TODO: Instead of echo serve as a chat.
