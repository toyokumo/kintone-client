(ns kintone-client.app
  (:require [kintone-client.constant.path.app :as path]
            [kintone-client.protocols :as pt]))

(defn get-app
  "Gets general information of an App, including the name, description, related Space, creator and updater information.

  app - The kintone app ID.
        integer"
  [conn app]
  (let [url (pt/-url conn path/app)]
    (pt/-get conn url {:params {:id app}})))

(defn get-form
  "Retrieves the form details of an app.
  WARN: This API is being deprecated.
  c.f. https://developer.kintone.io/hc/en-us/articles/213148927

  app - The kintone app ID.
        integer"
  [conn app]
  (let [url (pt/-url conn path/form)]
    (pt/-get conn url {:params {:app app}})))
