(ns echoecho.server
  (:import [java.nio.channels ServerSocketChannel]
           [java.net InetSocketAddress])
  (:gen-class))

(defn bind!
  "Takes a ServerSocket and a port number, then creates a Socket and
  binds the port to the Socket"
  [server-socket port]
  (doto server-socket
    (.socket)
    (.bind (InetSocketAddress. port))))

(defn unblock!
  [server-socket]
  (.configureBlocking server-socket false))

#_(defn open-server-socket
  [port]
  (let [server-socket (.open ServerSocketChannel)]
    (do
      (bind! server-socket port)
      (unblock! server-socket))
    (let [socket (socket server-socket)])
    ))

;; NOTE: I am unable to continue this project without knowing what
;; selectors and channel does on an OS level. Therefore writing some
;; notes here about it from internet so that I can use it later. This
;; will be removed after successful completion of this project.

;; Non blocking IO pipeline.
;; This is the chain of components that process non-blocking IO
;; Channel -> Selector -> Component -> Channel

;; A component uses selectors to check when a channel has data to
;; read. Then it will read from the channel and write it to a channel
;; itself. Look at the flow. Don't get this wrong. It is the component
;; that initiates the reading from the channel via selectors  and not
;; the other way around.

;; Why do we need to use selectors.?
;; In non blocking mode a stream may return either 0 or 1+ bytes. 0
;; means there is nothing to read. That is the stream has no data. There
;; will be multiple stream and we need to check whether each stream
;; returns 0 or 1+ bytes and read from the stream which returns 1+
;; bytes. To avoid checking this manually we use Selectors
;; One or more selectable channel instance can be registered with a
;; selector. When you call select() or selectNow(), it returns only
;; selectableChannel had data only.

;; Now we have bunch of selectableChannel after the select method and we
;; need to read data from it.
;; When we read data from the a  selectable channel we read it as
;; a block of data. This block of data may contain multiple
;; messages. Perhaps only partial message or perhaps more than one
;; message

;; Inorder to simplify this, we have one message reader for each channel
;; and the partial message will be stored somewhere else.
;; After retrieving a Channel instance which has data to read from the
;; Selector, the the Message Reader associated with that Channel reads
;; data and attempt to break it  into messages. If that results in any
;; full messages being read, these message can be passed down the read
;; pipeline to whatever component needs to process them
;; In essence,, the Message Reader will store the partial messages.

;; Here is an interesting thing. The message reader should be of the
;; size of the largest message that can be parsed. This is not
;; ideal. Therefore the buffer size of this message reader should be
;; dynamic. There are two ways to make the buffer size dynamic. One is
;; resize by copy and other one is resize by append. The disadvantage of
;; resize by copy is that there is always a copying operation when the
;; size of the buffer needs to be increased. Advantage, easy message
;; processing
;; Resize by append - the advantage is that there is no copy operation
;; bu disadvantage is that the parsing of message will be difficul.

;; TLV Encoded message - Some protocol message formats are encoded using
;; a TLV format (Type, Length, Value). That means, that when a message
;; arrives the total length of the message is stored in the beginning of
;; the message. That way you know immediately how much memory to
;; allocate for the whole message.

;; TLV encodings make memory management much easier. You know
;; immediately how much memory to allocate for the message. No memory is
;; wasted at the end of a buffer that is only partially used.

;; One disadvantage of TLV encodings is that you allocate all the memory
;; for a message before all the data of the message has arrived. A few,
;; slow connections sending big messages can thus allocate all the
;; memory you have available, making your server unresponsive.

;; A workaround for this problem would be to use a message format that
;; contains multiple TLV fields inside. Thus, memory is allocated for
;; each field, not for the whole message, and memory is only allocated
;; as the fields arrive. Still, a large field can have the same effect
;; on your memory management as a large message.

;; Just like Message Reader, we need message writer.
;; Message Writer manages partial messages more or less similarly how
;; Message Reader reads the data.

;; Now there is problem of selecting channel that is ready to write. Its
;; not like the case of reading. In reading we can identify which
;; channel has messages, but in writing there is no such thing.

;; Here's how its solved

;; When a message is written to a Message Writer, the Message Writer
;; registers its associated Channel with the Selector (if it is not
;; already registered).

;; When your server has time, it checks the Selector to see which of the
;; registered Channel instances are ready for writing. For each
;; write-ready Channel its associated Message Writer is requested to
;; write data to the Channel. If a Message Writer writes all its
;; messages to its Channel, the Channel is unregistered from the
;; Selector again.

;; Now read this once, the same process will repeat like a pipeline

;; int bytesRead = inChannel.read(buf); //read into buffer.
;; Basically means write

;; //read from buffer into channel.
;; int bytesWritten = inChannel.write (buf);
;; even though its write, basically what we are doing is reading from
;; the channel and putting it into the buffer.
