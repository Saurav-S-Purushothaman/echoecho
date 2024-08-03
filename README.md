# echoecho

As a starting step, this will act as an echo server that accepts just
one client.

The default port exposed is - 127.0.0.1:7007

## Getting Started

To start the program, clone this repository and run the below shell
command

``` shell
$ clj -M -m echoecho.echo
```

Connect to the server using any client.
Example using `telnet client`

``` shell
$ telnet localhost 7007
```

All send messages are echoed

``` shell
$ telnet localhost 7007
]
Trying ::1...
Connected to localhost.
Escape character is '^]'.
this
this
that
that
with
with
more
more
not
not
chat
chat
```
