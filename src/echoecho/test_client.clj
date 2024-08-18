(ns echoecho.test-client
  (:require [taoensso.timbre :as timbre])
  (:import
   [java.nio.channels
    SocketChannel]
   [java.nio ByteBuffer]
   [java.net InetSocketAddress])
  (:gen-class))


(defn connect-to-address!
  "Connects the server channel to an InetSocketAddress of the given port
  number"
  [server-channel port-number]
  (.connect server-channel (InetSocketAddress. port-number))
  (timbre/info "Connection Established Successfully"))


(defn write-buffer!
  "Writes the giveninput String to given buffer and convert the buffer
  to READ mode"
  [input buffer]
  (let [input-bytes (.getBytes input)]
    (.clear buffer)
    (.put buffer input-bytes)
    (.flip buffer)))


(defn write-channel!
  "Writes the given ByteBuffer to the given ServerChannel"
  [server-channel buffer]
  (loop []
    (when (.hasRemaining buffer)
      (.write server-channel buffer)
      (recur))))


(defn start-client
  [port-number]
  (try
    (let [server-channel (SocketChannel/open)
          _ (connect-to-address! server-channel port-number)
          buffer (ByteBuffer/allocate 1024)]
      (timbre/info "Connection established")
      (while true
        ;; This is blocking
        (let [input (read-line)]
          (when (= "quit" input)
            (System/exit 0))
          (write-buffer! input buffer)
          (write-channel! server-channel buffer))))
    (catch Exception e
      (timbre/info "Failed to start the server with Excetpion:" e)
      (throw (RuntimeException. e)))))


(defn -main
  [& args]
  (start-client 7007))
