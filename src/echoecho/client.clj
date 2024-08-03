(ns echoecho.client
  (:require [clojure.java.io :as io])
  (:import [java.net InetAddress]
           [java.net Socket])
  (:gen-class))

(def +running+ (atom true))

(defn read-input
  []
  (print "Send: ")
  (flush)
  (read-line))

(defn send-user-input!
  [socket input]
  ;; write to the socket
  (let [writer (io/writer socket)]
    (.write writer input)
    (.flush writer)))

(defn read-user-input!
  [socket]
  (let [user-input (read-input)]
    (send-user-input! socket user-input)))

(defn receive-server-ouput
  [socket]
  (let [reader (io/reader socket)]
    (print "Receive: ")
    (.readLine reader)))

(defn repeatedly-read-user-input!
  [socket]
  (future
    (loop []
      (when @+running+
        (read-user-input! socket)
        (println (receive-server-ouput socket)))
      (recur))))

(defn client
  "Given a part number, connect to that port and returns the socket"
  [port]
  (let [host-name (.getHostName (InetAddress/getLocalHost))
        socket (Socket. host-name port)]
    socket))

(defn chat
  [port]
  (let [socket (client 7007)]
    (repeatedly-read-user-input! socket)
    (Thread/sleep 10000)
    (.close socket)
    (swap! +running+ not)))

(defn -main
  [& _]
  (chat 7007))
