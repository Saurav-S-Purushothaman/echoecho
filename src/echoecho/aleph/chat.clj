(ns echoecho.aleph.chat
  (:require [echoecho.aleph.server :as server]
            [manifold.stream :as s])
  (:gen-class))

(def ^:const port 10000)
(def server (atom nil))
(def client (atom nil))

(defn echo
  "Echo the result"
  [arg]
  arg)

(defn handle-server!
  "Starts a server and store the state in the atom"
  [server]
  (reset! server (server/start-server! echo port)))

(defn messenger!
  []
  (println "Type your input: ")
  (flush)
  (let [input (read-line)]
    @(s/put! @client input)
    (println @(s/take! @client))))

(defn handle-client!
  "Connect to the localhost server. It also does more than connecting,
  it handles the user input and send it to the server and display the
  response"
  [client]
  (reset! client (server/connect-server! "localhost" port))
  (messenger!))

(defn respond! []
  (println "For being a server, press s")
  (println "For being a client, press c")
  (println "What are you? : ")
  (flush)
  (let [response (keyword (read-line))]
    (cond
      (identical? response :s) (handle-server! server)
      (identical? response :c) (handle-client! client)
      :else (do (println "Wrong input") (System/exit 0)))))

(defn -main
  [& args]
  (respond!))
