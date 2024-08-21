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
  (.accept server-socket))

(defn server
  [port handler]
  (let [chan (a/chan)
        server-socket (ServerSocket. port)
        socket (create-async-socket chan server-socket)
        socket-input-stream (DataInputStream. (.getInputStream socket))
        socket-output-stream (DataOutputStream. (.getOutputStream socket))
        msg-in (atom nil)
        msg-out (atom nil)]
    ;; Type /quit to quit the connection.
    ;; TODO: We need to change this imperative way of doing things to
    ;; functional sytel using recursion or sequence operation.
    (while (not= @msg-in  "/quit")
      (reset! msg-in (.readLine socket-input-stream))
      (reset! msg-out (handler @msg-in))
      (.writeBytes socket-output-stream @msg-out)
      (.flush socket-output-stream)
    (.close socket-input-stream)
    (.close socket))))

(defn -main
  [& _]
  (server 7007 echo))

;; TODO: Complete rewrite of server is required because this IO bound
;; channel is not compatible for listening to client that are non
;; blocking. Maybe we can create new file called server
