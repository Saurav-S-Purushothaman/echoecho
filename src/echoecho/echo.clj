(ns echoecho.echo
  (:require [clojure.core.async :as a :refer [>!! <!! >! <!]])
  (:import [java.io
            DataOutputStream
            DataInputStream]
           [java.net
            ServerSocket
            Socket])
  (:gen-class))

(defn echo
  "Returns the text by adding new line"
  [arg]
  (str arg "\n"))

(defn socket-open? [^Socket socket]
  (not (or (.isClosed socket)
           (.isInputShutdown socket)
           (.isOutputShutdown socket))))

(defn create-async-socket
  [chan server-socket]
  (.accept server-socket))

(defn get-async-socket [chan]
  (<!! chan))

;; Make socket as an atom with global scope
(def socket-state (atom nil))
;; Connect to the server only if we put something on the channel
;; Lets call the channel server

(def server (a/chan))

;; Now lets create the function that accepts a socket asynchronously
(defn create-server [server-socket]
  (a/go-loop []
    (reset! socket-state (.accept server-socket))
    (prn "Accepted the Socket")
    (println "Marking the acceptance of the socket with: " (<! server))
    (recur)))

;; now put something on the channel to let the create-serer code run.
(defn run-create-server
  []
  (>!! server true)
  true)

;; Once the socket connection is accepted, loop the serve code

(defn server
  [port handler]
  (let [server-socket (ServerSocket. port)]
    (create-server server-socket)
    (run-create-server))
  (let [chan (a/chan)
        server-socket (ServerSocket. port)
        socket (create-async-socket chan server-socket)
        socket-input-stream (DataInputStream. (.getInputStream @socket))
        socket-output-stream (DataOutputStream. (.getOutputStream @socket))
        msg-in (atom nil)
        msg-out (atom nil)
        ;; For now, lets support only two clients
        _ (run-create-server)]
    ;; Type /quit to quit the connection.
    (while (not= @msg-in  "/quit")
      (reset! msg-in (.readLine socket-input-stream))
      (reset! msg-out (handler @msg-in))
      (.writeBytes socket-output-stream @msg-out)
      (.flush socket-output-stream))))

(defn -main
  [& _]
  (server 7007 echo))
