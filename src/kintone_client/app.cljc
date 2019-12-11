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

(defn get-apps
  ""
  [conn {:keys [offset limit codes name ids space-ids]}]
  (let [params (cond-> {}
                 (some? offset) (assoc :offset offset)
                 (some? limit) (assoc :limit limit)
                 (seq codes) (assoc :codes codes)
                 (some? name) (assoc :name name)
                 (seq ids) (assoc :ids ids)
                 (seq space-ids) (assoc :spaceIds space-ids))
        url (pt/-url conn path/apps)]
    (pt/-get conn url {:params params})))

(defn get-form-layout
  ""
  [conn app {:keys [is-preview?]}]
  (let [url (pt/-url conn (if is-preview?
                            path/preview-form-layout
                            path/form-layout))]
    (pt/-get conn url {:params {:app app}})))

(defn update-form-layout
  ""
  [conn app layout {:keys [revision]}]
  (let [params (cond-> {:app app :layout layout}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-layout)]
    (pt/-put conn url {:params params})))

(defn get-form-fields
  ""
  [conn app {:keys [lang is-preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if is-preview?
                            path/preview-form-fields
                            path/form-fields))]
    (pt/-get conn url {:params params})))

(defn add-form-fields
  ""
  [conn app fields {:keys [revision]}]
  (let [params (cond-> {:app app :properties fields}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-post conn url {:params params})))

(defn update-form-fields
  ""
  [conn app fields {:keys [revision]}]
  (let [params (cond-> {:app app :properties fields}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-put conn url {:params params})))

(defn delete-form-fields
  ""
  [conn app codes {:keys [revision]}]
  (let [params (cond-> {:app app :fields codes}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-delete conn url {:params params})))

(defn add-preview-app
  ""
  [conn {:keys [name space thread]}]
  (let [params (cond-> {:name name}
                 (some? space) (assoc :space space)
                 (some? thread) (assoc :thread thread))
        url (pt/-url conn path/preview-app)]
    (pt/-post conn url {:params params})))

(defn deploy-app-settings
  ""
  [conn apps {:keys [revert]}]
  (let [params (cond-> {:apps apps}
                 (some? revert) (assoc :revert revert))
        url (pt/-url conn path/preview-deploy)]
    (pt/-post conn url {:params params})))

(defn get-app-deploy-status
  ""
  [conn apps]
  (let [params {:apps apps}
        url (pt/-url conn path/preview-deploy)]
    (pt/-get conn url {:params params})))

(defn get-views
  ""
  [conn app {:keys [lang is-preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if is-preview?
                            path/preview-views
                            path/views))]
    (pt/-get conn url {:params params})))

(defn update-views
  ""
  [conn app views {:keys [revision]}]
  (let [params (cond-> {:app app :views views}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-views)]
    (pt/-put conn url {:params params})))

(defn get-general-settings
  ""
  [conn app {:keys [lang is-preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if is-preview?
                            path/preview-settings
                            path/settings))]
    (pt/-get conn url {:params params})))

(defn update-general-settings
  ""
  [conn app settings {:keys [revision]}]
  (let [params (cond-> (merge {:app app} settings)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-settings)]
    (pt/-put conn url {:params params})))

(defn get-status
  ""
  [conn app {:keys [lang is-preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if is-preview?
                            path/preview-status
                            path/status))]
    (pt/-get conn url {:params params})))

(defn update-status
  ""
  [conn app status {:keys [revision]}]
  (let [params (cond-> (merge {:app app} status)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-status)]
    (pt/-put conn url {:params params})))

(defn get-customize
  ""
  [conn app {:keys [is-preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if is-preview?
                            path/preview-customize
                            path/customize))]
    (pt/-get conn url {:params params})))

(defn update-customize
  ""
  [conn app customize {:keys [revision]}]
  (let [params (cond-> (merge {:app app} customize)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-customize)]
    (pt/-put conn url {:params params})))

(defn get-acl
  ""
  [conn app {:keys [is-preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if is-preview?
                            path/preview-acl
                            path/acl))]
    (pt/-get conn url {:params params})))

(defn update-acl
  ""
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-acl)]
    (pt/-put conn url {:params params})))

(defn get-field-acl
  ""
  [conn app {:keys [is-preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if is-preview?
                            path/preview-field-acl
                            path/field-acl))]
    (pt/-get conn url {:params params})))

(defn update-field-acl
  ""
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-field-acl)]
    (pt/-put conn url {:params params})))

(defn update-live-field-acl
  ""
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/field-acl)]
    (pt/-put conn url {:params params})))
