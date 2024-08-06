(ns echoecho.client
  (:import [java.io
            OutputStreamWriter
            PrintWriter
            BufferedReader
            InputStreamReader]
           [java.net
            InetAddress
            Socket])
  (:gen-class))

(defn localhost []
  (InetAddress/getLocalHost))

(defn host-name [^InetAddress address]
  (.getHostName address))

(defn read-input!
  "Returns user-input. This blocsk the current thread."
  []
  (print "Send: ")
  (flush)
  (read-line))

(defn socket-input-stream
  "Returns a BufferedReader of Socket's InputStream."
  [socket]
  (BufferedReader.
   (InputStreamReader.
    (.getInputStream socket))))

(defn socket-output-stream
  "Returns a PrintWriter of Socket's OutputStream."
  [socket]
  (PrintWriter.
   (OutputStreamWriter.
    (.getOutputStream socket))))

(defn client!
  [port]
  (let [host-name (host-name (localhost))
        socket (Socket. host-name port)
        user-input (atom nil) ;; This will block the current thread
        socket-input-stream (socket-input-stream socket)
        socket-output-stream (socket-output-stream socket)
        msg (atom nil)]
    (while (not= "quit" @msg)
      ;; Get user input in an atom and block the current thread
      (reset! user-input (read-input!))
      ;; Write the user input to the socket output stream. The println
      ;; method is not connected to stdout, therefore it won't print
      ;; anything on stdout
      (.println socket-output-stream @user-input)
      (.flush socket-output-stream)
      (reset! msg (.readLine socket-input-stream))
      (print "Receive:" @msg "\n"))
    (.close socket-output-stream)
    (.close socket)))

(defn -main
  [& _]
  (client! 7007))
