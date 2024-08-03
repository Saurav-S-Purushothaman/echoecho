(ns echoecho.echo
  "This is will be a temporary main file"
  (:require [clojure.java.io :as io]
            [taoensso.timbre :as timbre])
  (:import [java.net ServerSocket]
           [java.net Socket])
  (:gen-class))

(defn receive!
  "Read a line of textual data from the given socket"
  [socket]
  ;; NOTE: There is no need to flush the reader because reader is not
  ;;       using a buffered stream to read from the socket instead it
  ;;       is read directly from the socket. Moreover readLine is a
  ;;       blocking method.
  (.readLine (io/reader socket)))

(defn send!
  "Sends the given textual data to the given socket"
  [socket text]
  ;; NOTE: We need to flush the writer, because the writer uses
  ;;       BufferedStream for efficiency. Flush will make sure that
  ;;       all the data will be send back to the client instead of
  ;;       being buffered into the writer instance
  (let [writer (io/writer socket)]
    (.write writer text)
    (.flush writer)))

(defn echo
  "Returns the text by adding new line"
  [arg]
  (str arg "\n"))

(defn serve!
  "Handles creating an instance of ServerSocket on a particular port.
  Takes a handler function, which will be used to process the incoming
  request and determine a response message"
  [port handler]
  (let [server-socket (ServerSocket. port)
        ;; Calls server-sockets accept method. Blocks until
        ;; sessixon is established. Returs Socket instance upon
        ;; successful connection of client
        socket (.accept server-socket)
        msg-in (atom (receive! socket))
        msg-out (handler @msg-in)]
    (timbre/info "Session between client and server established on port:"
                 (.getPort socket))
    (loop []
      (when-not (.isClosed socket)
        (send! socket msg-out))
      (recur))))

(defn -main
  "The entry point of our application"
  [& _]
  (serve! 7007 echo))
