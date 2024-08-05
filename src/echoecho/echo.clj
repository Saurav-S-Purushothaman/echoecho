(ns echoecho.echo
  (:require [clojure.core.async :as a :refer [>!! <!! >! <!]])
  (:import [java.io
            DataOutputStream
            DataInputStream]
           [java.net
            ServerSocket])
  (:gen-class))

(defn echo
  "Returns the text by adding new line"
  [arg]
  (str arg "\n"))

(defn create-async-socket [chan server-socket]
  (a/thread (>!! chan (.accept server-socket))))

(defn get-async-socket [chan]
  (<!! chan))

(defn server
  [port handler]
  (while true
    (let [chan (a/chan)
          server-socket (ServerSocket. port)
          _ (create-async-socket chan server-socket)
          socket (get-async-socket chan)
          socket-input-stream (DataInputStream. (.getInputStream socket))
          socket-output-stream (DataOutputStream. (.getOutputStream socket))
          msg-in (atom nil)
          msg-out (atom nil)]
      (create-async-socket chan server-socket)
      ;; Type /quit to quit the connection.
      (while (not= @msg-in  "/quit")
        (reset! msg-in (.readLine socket-input-stream))
        (reset! msg-out (handler @msg-in))
        (.writeBytes socket-output-stream @msg-out)
        (.flush socket-output-stream))
      (.close socket-input-stream)
      (.close socket))))

(defn -main
  [& _]
  (server 7007 echo))

;; TODO: Add for multiclient support
;; What we really need to implement is non blocking io operation
