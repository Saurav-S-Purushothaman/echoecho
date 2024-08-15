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
  [key]
  (cast java.nio.channels.SocketChannel (.channel key)))

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


;; Storing all the clients in an atom to close it finally.
(defonce clients-state (atom #{}))

(defn start-server
  [port-number]
  (try
    (let [server-socket-channel (ServerSocketChannel/open)
          selector (Selector/open)
          inet-address (InetSocketAddress. port-number)
          clients #{}]
      (.configureBlocking server-socket-channel false)
      (.bind server-socket-channel inet-address)
      (.register server-socket-channel selector (SelectionKey/OP_ACCEPT))
      (loop []
        ;; this is blocking
        (if (zero? (.select selector))
          (recur)
          (do
            (doseq [key (.selectedKeys selector)]
              (let [key-channel (.channel key)]
                (timbre/info "Got Channel")
                (timbre/info "The key is : " key)
                (timbre/info "Type of key is " (class key))
                (timbre/info "Type of key.channel is " (class key-channel))
                ;; The key is only acceptable, if the key is pointing to
                ;; an accept on a socket
                (when (.isAcceptable key)
                  (when (instance? key-channel ServerSocketChannel)
                    ;; We will see if this let is necessary
                    (let [channel (get-socket-channel key)
                          ;; this is non blocking
                          client (.accept channel)
                          client-socket (.socket client)]
                      (client-info client-socket)
                      ;; Configure blocking for the socket and also
                      ;; register the client on the same selector but
                      ;; with read key
                      (.configureBlocking client false)
                      (.register selector (SelectionKey/OP_READ) client)
                      (swap! clients-state conj client))
                    true)))
              (doto selector (.selectedKeys) (.clear)))))))
    (catch Exception e
      (timbre/error "Server open failed with Exception: " e)
      (throw (RuntimeException. e)))
    (finally
      (try
        (doseq [x @clients-state]
          (.close x))
        (catch Exception e
          (timbre/error "Failed to close the connection with Exception" e)
          (throw (RuntimeException. e)))))))

(defn -main
  [& args]
  (start-server 7007))

(comment
  (start-server 7007))
