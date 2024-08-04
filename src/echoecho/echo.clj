(ns echoecho.echo
  (:import
   [java.io
    DataOutputStream
    DataInputStream
    BufferedReader
    InputStreamReader]
   [java.net
    ServerSocket
    Socket])
  (:gen-class))

(defn echo
  "Returns the text by adding new line"
  [arg]
  (str arg "\n"))

(defn server
  [port handler]
  (let [server-socket (ServerSocket. port)
        socket (.accept server-socket)
        socket-input-stream (DataInputStream. (.getInputStream socket))
        socket-output-stream (DataOutputStream. (.getOutputStream socket))
        msg-in (atom nil)
        msg-out (atom nil)]
    ;; Type /quit to quit the connection.
    (while (not= @msg-in  "/quit")
      (reset! msg-in (.readLine socket-input-stream))
      (reset! msg-out (handler @msg-in))
      (.writeBytes socket-output-stream @msg-out)
      (.flush socket-output-stream))
    (.close socket-input-stream)
    (.close socket)))

(defn -main
  [& _]
  (server 7007 echo))
