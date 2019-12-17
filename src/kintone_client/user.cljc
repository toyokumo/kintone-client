(ns kintone-client.user
  (:require [kintone-client.constant.path.user :as path]
            [kintone-client.protocols :as pt]))

(defn get-users
  ""
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/users) {:params params})))

(defn add-users
  ""
  [conn users]
  (pt/-post conn (pt/-user-api-url conn path/users) {:params {:users users}}))

(defn update-users
  ""
  [conn users]
  (pt/-put conn (pt/-user-api-url conn path/users) {:params {:users users}}))

(defn delete-users
  ""
  [conn user-codes]
  (pt/-delete conn (pt/-user-api-url conn path/users) {:params {:codes user-codes}}))

(defn update-user-codes
  ""
  [conn codes]
  (pt/-put conn (pt/-user-api-url conn path/user-codes) {:params {:codes codes}}))

(defn get-organizations
  ""
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/organizations) {:params params})))

(defn add-organizations
  ""
  [conn organizations]
  (pt/-post conn (pt/-user-api-url conn path/organizations) {:params {:organizations organizations}}))

(defn update-organizations
  ""
  [conn organizations]
  (pt/-put conn (pt/-user-api-url conn path/organizations) {:params {:organizations organizations}}))

(defn delete-organizations
  ""
  [conn org-codes]
  (pt/-delete conn (pt/-user-api-url conn path/organizations) {:params {:codes org-codes}}))

(defn update-org-codes
  ""
  [conn codes]
  (pt/-put conn (pt/-user-api-url conn path/organization-codes) {:params {:codes codes}}))

(defn update-user-services
  ""
  [conn users]
  (pt/-put conn (pt/-user-api-url conn path/user-services) {:params {:users users}}))

(defn get-groups
  ""
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/groups) {:params params})))

(defn get-user-organizations
  ""
  [conn user-code]
  (pt/-get conn (pt/-user-api-url conn path/user-organizations) {:params {:code user-code}}))

(defn get-user-groups
  ""
  [conn user-code]
  (pt/-get conn (pt/-user-api-url conn path/user-groups) {:params {:code user-code}}))

(defn get-organization-users
  ""
  [conn code {:keys [offset size]}]
  (let [params (cond-> {:code code}
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/organization-users) {:params params})))

(defn get-group-users
  ""
  [conn code {:keys [offset size]}]
  (let [params (cond-> {:code code}
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/group-users) {:params params})))

