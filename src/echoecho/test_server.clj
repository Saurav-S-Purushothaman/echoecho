(ns echoecho.test-server
  (:import [java.nio.channels
            ServerSocketChannel
            Selector
            SelectionKey]
           [java.net
            InetSocketAddress])
  (:require [taoensso.timbre :as timbre])
  (:gen-class))


(defn start-server
  [port-number]
  (try
    (let [server-socket-channel (ServerSocketChannel/open)
          selector (Selector/open)
          inet-address (InetSocketAddress. port-number)]
      (.configureBlocking server-socket-channel false)
      (.bind server-socket-channel inet-address)
      ;; Register the server-socket-channel with the selector. It will,
      ;; when a server socket channel has an Operation Accept method
      ;; ready to be processed or polled, fire the selector
      (.register server-socket-channel selector (SelectionKey/OP_ACCEPT))
      (loop []
        (if (zero? (.select selector))
          (recur)
          (do
            (doseq [key (.selectedKeys selector)]
              ;; Just log the key right now, we will handle this later
              (timbre/info "The server started")
              (timbre/info key)
              (if (instance? (.channel key) ServerSocketChannel)
              ;; The key is only acceptable, if the key is pointing to
              ;; an accept on a socket
                (let [channel (.channel key)]
                  (println (type channel)))
              (if (.isAcceptable key)
                true
                false))
            (doto selector (.selectedKeys) (.clear)))))))
    (catch Exception e
      (timbre/error "Server open failed with Exception: " e)
      (throw (RuntimeException. e)))))
