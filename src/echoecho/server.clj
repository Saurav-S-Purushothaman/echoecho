(ns echoecho.server
  (:import [java.nio.channels ServerSocketChannel]
           [java.net InetSocketAddress])
  (:gen-class))

(defn socket
  "Takes a ServerSocket and a port number, then creates a Socket and
  binds the port to the Socket"
  [server-socket port]
  (doto server-socket
    (.socket)
    (.bind (InetSocketAddress. port))))

(defn listen-to-connection!
  [socket]
  (while true
    (.accept)))

(defn open-server-socket
  [port]
  (let [server-socket (.open ServerSocketChannel)
        ;; Bind the server socket to the port. I am not sure what bind
        ;; will return therefore doing this in seperate function call. I
        ;; am too lazy to look at the docs lmao.
        _ (socket server-socket port)]

    #_(.connect )))
