(ns kintone.protocols)

(defprotocol IAuth
  (-header [_]))

(defprotocol IRequest
  "HTTP request abstraction."
  (-path [_ path])
  (-url [_ path])
  (-get [_ url req])
  (-post [_ url req])
  (-put [_ url req])
  (-delete [_ url req]))
