(ns echoecho.test-server
  (:import [java.nio.channels
            ServerSocketChannel
            Selector
            SelectionKey]
           [java.net
            InetSocketAddress])
  (:require [taoensso.timbre :as timbre])
  (:gen-class))

(defn get-socket-channel
  [key-channel]
  (cast java.nio.channels.ServerSocketChannel key-channel))

(defn client-host
  [client]
  (doto client
    (.getInetAddress)
    (.getHostAddress)))

(defn client-port
  [client]
  (.getPort client))

(defn client-info
  [client-socket]
  (let [host-address (client-host client-socket)
        port (client-port client-socket)
        log {:host host-address
             :port port}]
    (timbre/info "Connected:" log)
    (timbre/info "Type of client socket :" (type client-socket))))

(defn initialize-server-socket!
  "Sets up the server socket channel for non-blocking operations, binds
  it to the given address, and registers it with the selector for
  accepting connections."
  [server-socket-channel inet-address selector]
  (.configureBlocking server-socket-channel false)
  (.bind server-socket-channel inet-address)
  (.register server-socket-channel selector (SelectionKey/OP_ACCEPT)))

(defn initialize-client-socket!
  "Sets up the server socket channel for non-blocking operations,
  registers it with the selector for accepting connections and adds the
  client to clients-state atom"
  [client selector clients-state]
  (.configureBlocking client false)
  (.register client selector (SelectionKey/OP_READ))
  (swap! clients-state conj client))

(defn close-client-socket!
  [clients-state]
  (try
    (doseq [x @clients-state]
      (.close x))
    (catch Exception e
      (timbre/error "Failed to close the connection with Exception" e)
      (throw (RuntimeException. e)))))


;; Storing all the clients in an atom to close it finally.
(defonce clients-state (atom #{}))

(defn start-server
  [port-number]
  (try
    (let [server-socket-channel (ServerSocketChannel/open)
          selector (Selector/open)
          inet-address (InetSocketAddress. port-number)]
      (initialize-server-socket! server-socket-channel inet-address selector)
      (loop []
        ;; this is blocking
        (if (zero? (.select selector))
          (do
            (println "Closing the system")
            (System/exit 0))
          (doseq [key (.selectedKeys selector)]
            (let [key-channel (.channel key)]
              (when (.isAcceptable key)
                (let [;; this is non blocking
                      key-channel' (get-socket-channel key-channel)
                      client (.accept key-channel')]
                  ;; Register the client on the same selector but
                  ;; with read key
                  (initialize-client-socket! client selector clients-state)
                  (.register client selector (SelectionKey/OP_READ))
                  (swap! clients-state conj client))))))
        (recur)))
    (catch Exception e
      (timbre/error "Server open failed with Exception: " e)
      (throw (RuntimeException. e)))
    (finally (close-client-socket! clients-state))))

(defn -main
  [& args]
  (start-server 7007))

(comment
  (start-server 7007))
