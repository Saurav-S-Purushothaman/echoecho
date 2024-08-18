(ns echoecho.test-server
  (:import
   [java.nio.channels
    SocketChannel
    ServerSocketChannel
    Selector
    SelectionKey]
   [java.nio ByteBuffer]
   [java.net
    InetSocketAddress])
  (:require
   [taoensso.timbre :as timbre])
  (:gen-class))


(defonce clients-state (atom #{}))


(defn get-socket-channel
  [key-channel]
  (cast java.nio.channels.ServerSocketChannel key-channel))


(defn get-socket
  [key-channel]
  (cast java.nio.channels.SocketChannel key-channel))


(defn client-port
  [client]
  (.getPort client))


(defn initialize-server-socket!
  [server-socket-channel inet-address selector]
  (-> server-socket-channel
      (.configureBlocking false))
  (-> server-socket-channel
      (.bind inet-address))
  (-> server-socket-channel
      (.register selector (SelectionKey/OP_ACCEPT))))


(defn initialize-client-socket!
  [client selector clients-state]
  (-> client
      (.configureBlocking false))
  (-> client
      (.register selector (SelectionKey/OP_READ)))
  (swap! clients-state conj client))


(defn close-client-socket!
  [clients-state]
  (try
    (doseq [x @clients-state]
      (.close x))
    (catch Exception e
      (timbre/error "Failed to close the connection with Exception" e)
      (throw (RuntimeException. e)))))


(defn close-system!
  []
  (do (println "Closing the system") (System/exit 0)))


(defn read-channel-or-close-it!
  "Read from the socket and put into the byte. If read not available
  close the channel"
  [client-channel buffer]
  (let [bytes-read (.read client-channel buffer)]
    (when (= -1 bytes-read)
      (timbre/info "Disconnected")
      (.close client-channel)
      (swap! clients-state disj client-channel))
    bytes-read))


(defn buffer->string!
  [buffer bytes-read]
  (.flip buffer)
  (String. (.array buffer) (.position buffer) bytes-read)
  (.clear buffer))



(defn start-server
  [port-number]
  (try
    (let [server-socket-channel (ServerSocketChannel/open)
          selector (Selector/open)
          inet-address (InetSocketAddress. port-number)
          buffer (ByteBuffer/allocate 1024)]
      (initialize-server-socket! server-socket-channel inet-address selector)
      (loop []
        (if (zero? (.select selector)) ; Blocking
          (close-system!)
          (doseq [key (.selectedKeys selector)]
            (let [key-channel (.channel key)]
              (when (.isAcceptable key)
                ; Non blocking
                (let [key-channel' (get-socket-channel key-channel)
                      client (.accept key-channel')]
                  ;; Register the client on the same selector but
                  ;; with read key
                  (initialize-client-socket! client selector clients-state)
                  (.register client selector (SelectionKey/OP_READ))
                  (swap! clients-state conj client)))
              (when (.isReadable key)
                (let [client-channel (get-socket key-channel)
                      bytes-read (read-channel-or-close-it! client-channel buffer)
                      data (buffer->string! buffer bytes-read)]
                  (prn data))))))
        (recur)))
    (catch Exception e
      (timbre/error "Failed to open server" e)
      (throw (RuntimeException. e)))
    (finally (close-client-socket! clients-state))))


(defn -main
  [& args]
  (start-server 7007))


(comment
  (start-server 7007))
