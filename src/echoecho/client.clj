(ns echoecho.client
  (:import [java.io
            OutputStreamWriter
            PrintWriter
            BufferedReader
            InputStreamReader]
           [java.nio ByteBuffer]
           [java.nio.channels SocketChannel]
           [java.nio.channels.spi SelectorProvider]
           [java.net
            InetAddress
            InetSocketAddress])
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

(defn read-socket-channel
  "Takes a socket and data from socket is read into a
  ByteBuffer. Returns the buffer with whatever text is read."
  [socket]
  (let [buff (ByteBuffer/allocate 48)
        n-bytes (.read socket buff)]
    (when (and (some? n-bytes) (= 0 n-bytes))
      (println "The server did not respond anything.")
      (throw (Exception. "The server did not respond")))
    buff))

(defn write-socket-channel
  "Given a text and socket, puts the text into a byte buffer and then
  into the socket channel."
  ;; TODO: I should probably pass the buffer too an arg and not
  ;; create a local scoped one inside the function
  [socket text]
  ;; TODO: I should probably use clojure datastructure instead of java
  ;; interop as java objects are mutable.
  (let [bytes (.getBytes text)
        buff (ByteBuffer/allocate 48)]
    (.clear buff)
    (.put buff (.getBytes text))
    ;; set the limit to current position and current position to zero
    (.flip buff)
    (loop [socket socket buff buff]
      (when (.hasRemaining buff)
        (.write socket buff)
        (recur socket buff)))))

(defn connect-to-socket!
  [socket inet-address]
  (try
    (when (.connect socket inet-address)
      (println "Successful connection."))
    (catch Exception e
      (println "Connection refused due to excepiton " e)
      (throw (Exception. "Unable to connect to the server")))))

(defn client!
  [port]
  (let [host-name' (host-name (localhost))
        inet-socket-address (InetSocketAddress. host-name' port)
        socket (SocketChannel/open)
        _ (connect-to-socket! socket inet-socket-address)
        user-input (atom nil)
        msg (atom nil)]
    (while (not= "quit" @msg)
      (reset! user-input (read-input!))
      (write-socket-channel socket @user-input)
      (reset! msg (String. (read-socket-channel socket)))
      (print "Receive:" @msg "\n"))
    (.close socket)))

(defn -main
  [& _]
  (client! 7007))

;; TODO
;; Change the way you read from the socket. Reading requires
;; .read method
