(ns echoecho.aleph.server
  (:require
   [aleph.tcp :as tcp]
   [clojure.edn :as edn]
   [gloss.core :as gloss]
   [gloss.io :as io]
   [manifold.deferred :as d]
   [manifold.stream :as s])
  (:gen-class))


(comment
  (clojure.repl/doc gloss/compile-frame)
  (clojure.repl/doc gloss/finite-frame)
  (clojure.repl/doc s/stream)
  (clojure.repl/doc s/connect)
  (clojure.repl/doc s/splice)
  (clojure.repl/doc d/chain)
  (clojure.repl/source d/chain)
  (clojure.repl/source s/stream)
  (clojure.repl/source s/map)
  )

;; Gloss is for serialising and deserializing clojure datastructure.
;; This protocol works as follows:
;; Encoding: You give it a Clojure data structure.  pr-str turns that
;; data structure into a string.  The string's length is calculated, and
;; both the length (as a 32-bit integer) and the string are encoded into
;; bytes.  Decoding: It reads the first 32 bits (4 bytes) to get the
;; length of the string.  It then reads the next length bytes as a
;; string.  edn/read-string is applied to this string to convert it back
;; into a Clojure data structure.

;; This is super convineint
(def protocol (gloss/compile-frame (gloss/finite-frame :uint32
                                                       (gloss/string :utf-8))
                                   ;; Encoding
                                   pr-str
                                   ;; Decoding
                                   edn/read-string))

(defn wrap-duplex-stream
  "Connect the manifold stream with raw stream. Encodes all the
  messages in out before passing it to raw-stream. Also splice together
  the sink and source into a single duplex stream

  *Args*
  protocol - gloss encoder and decoder
  raw-stream - TCP duplex stream which is used to send and receive
  messages from TCP connection. This represent the connection through
  which data is sent and received"
  [protocol raw-stream]
  ;; out is a manifold stream (an abstraction for stream)
  (let [out (s/stream)]
    ;; See here, we are encoding the out with the protocol defined
    ;; before connecting the stream. Thereby before giving all the
    ;; stream from out to raw-stream, it is encoded.
    (s/connect (s/map #(io/encode protocol %) out) raw-stream)
    ;; splice together the sink and source into a single duplex stream
    ;; meaning all the message enqueued by put! go into sink and all
    ;; messages dequeued comes from the source
    (s/splice out (io/decode-stream raw-stream protocol))))

(defn client
  "Connect to the server and return a single duplex stream in deferred.
  Explanation: tcp/client returns a manifold deferred which yields a
  duplex stream. We asynchronously compose this using d/chain which will
  wait for the client to realize and then pass the client to wrap duplex
  stream"
  [host port]
  (d/chain (tcp/client {:host host :port port})
           #(wrap-duplex-stream protocol %)))

(defn start-server
  "Creates the server. Takes a handler function as arg, which takes two
  args i.e, a raw-stream and info (information about the connection) and
  sets up the stream for taking message."
  [port handler]
  (tcp/start-server (fn [raw-stream info]
                      ;; raw-stream is wrapped in gloss protocol before
                      ;; being handed out to handler function to handle
                      (handler (wrap-duplex-stream protocol raw-stream)
                               info))
                    {:port port}))


(defn start-server
  [handler port]
  (tcp/start-server (fn [s info]
                      (handler (wrap-duplex-stream protocol s) info))
                    {:port port}))
;;;;; Echo serer starts here ;;;;;


(defn echo-handler
  "Creates a handler function which will apply the `f` to any incoming
  message, and immediately send back the resul."
  [f]
  (fn [raw-stream info]
    ;; We are connecting the stream to itself
    (s/connect (s/map f raw-stream) raw-stream)))


;; Starting the server
(def server
  (start-server (echo-handler inc) 10000))

;; We connect a client to the server, dereferencing the deferred value
;; returned such that `c` is simply a duplex stream that takes and emits
;; Clojure values.
(def c @(client "localhost" 10000))

@(s/put! c 1)
@(s/take! c)
