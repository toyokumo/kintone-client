(ns kintone-client.user
  (:require #?(:clj  [clojure.core.async :refer [go go-loop <!]]
               :cljs [cljs.core.async :refer [<!] :refer-macros [go go-loop]])
            [kintone-client.constant.path.user :as path]
            [kintone-client.protocols :as pt]
            [kintone-client.types :as t]))

(defn get-users
  "Gets information of users.

  opts

    :ids - A list of User IDs.
           sequence of integer

    :codes - A list of User Codes (log-in names).
             sequence of string

    :offset - The offset.
              The default value is 0 if this is not assigned.
              integer

    :size - The maximum number of User information to get.
            The default value is 100 if this is not assigned.
            integer"
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/users) {:params params})))

(defn add-users
  "Adds users to a kintone environment.

  users - See API reference regarding user format.
          sequence of map"
  [conn users]
  (pt/-post conn (pt/-user-api-url conn path/users) {:params {:users users}}))

(defn update-users
  "Updates users of a kintone environment.

   users - See API reference regarding user format.
           sequence of map"
  [conn users]
  (pt/-put conn (pt/-user-api-url conn path/users) {:params {:users users}}))

(defn delete-users
  "Deletes users from a Kintone environment.

  user-codes - An array of user codes of users to be deleted.
               sequence of string"
  [conn user-codes]
  (pt/-delete conn (pt/-user-api-url conn path/users) {:params {:codes user-codes}}))

(defn update-user-codes
  "Updates user codes of users.

  codes - sequence of map
          A map includes these keys
            :currentCode - The current User Code (log-in name).
                           string

            :newCode - The new User Code (log-in name).
                       string"
  [conn codes]
  (pt/-put conn (pt/-user-api-url conn path/user-codes) {:params {:codes codes}}))

(defn get-organizations
  "Gets information of organizations.

  opts

    :ids - A list of Organization IDs.
           sequence of integer

    :codes - A list of Organization Codes.
             sequence of string

    :offset - The offset.
              The default value is 0 if this is not assigned.
              integer

    :size - The maximum number of Organization information to get.
            The default value is 100 if this is not assigned.
            integer"
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/organizations) {:params params})))

(defn add-organizations
  "Adds organizations to a kintone environment.

  organizations - See API reference regarding organization format.
                  sequence of map"
  [conn organizations]
  (pt/-post conn (pt/-user-api-url conn path/organizations) {:params {:organizations organizations}}))

(defn update-organizations
  "Updates organizations of a Kintone environment.

  organizations - See API reference regarding organization format.
                  sequence of map"
  [conn organizations]
  (pt/-put conn (pt/-user-api-url conn path/organizations) {:params {:organizations organizations}}))

(defn delete-organizations
  "Deletes organizations from a Kintone environment.

  org-codes - An array of organization codes of organization to be deleted.
              sequence of string"
  [conn org-codes]
  (pt/-delete conn (pt/-user-api-url conn path/organizations) {:params {:codes org-codes}}))

(defn update-organization-codes
  "Updates organization codes of organizations.

  codes - sequence of map
          A map includes these keys
            :currentCode - The current Organization Code.
                           string

            :newCode - The new Organization Code.
                       string"
  [conn codes]
  (pt/-put conn (pt/-user-api-url conn path/organization-codes) {:params {:codes codes}}))

(defn update-user-services
  "Updates the services that users can use in a kintone environment.

  users - See API reference regarding user services format.
          sequence of map"
  [conn users]
  (pt/-put conn (pt/-user-api-url conn path/user-services) {:params {:users users}}))

(defn get-groups
  "Gets information of groups.

  opts

    :ids - A list of Group IDs.
           sequence of integer

    :codes - A list of Group Code.
             sequence of string

    :offset - The offset.
              The default value is 0 if this is not assigned.
              integer

    :size - The maximum number of Group information to get.
            The default value is 100 if this is not assigned.
            integer"
  [conn {:keys [ids codes offset size]}]
  (let [params (cond-> {}
                 (some? ids) (assoc :ids ids)
                 (some? codes) (assoc :codes codes)
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/groups) {:params params})))

(defn get-user-organizations
  "Gets information of organizations that a User belongs to, and Job Title information related to the organization.

  user-code - The User's code (log-in name).
              string"
  [conn user-code]
  (pt/-get conn (pt/-user-api-url conn path/user-organizations) {:params {:code user-code}}))

(defn get-user-groups
  "Gets information of Groups that a User belongs to.

  user-code - The User's code (log-in name)
              string"
  [conn user-code]
  (pt/-get conn (pt/-user-api-url conn path/user-groups) {:params {:code user-code}}))

(defn get-organization-users
  "Gets information of users that belong to the organization. Each of userTitles may have \"title\" of the job

  code - The organization code.
         string

  opts

    :offset - The offset.
              The default value is 0 if this is not assigned.
              integer

    :size - The maximum number of Organization information to get.
            The default value is 100 if this is not assigned.
            integer"
  [conn code {:keys [offset size]}]
  (let [params (cond-> {:code code}
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/organization-users) {:params params})))

(defn get-group-users
  "Gets information of Users that belong to a Group.

  code - The group code.
         string

  opts

    :offset - The offset.
              The default value is 0 if this is not assigned.
              integer

    :size - The maximum number of Group information to get.
            The default value is 100 if this is not assigned.
            integer"
  [conn code {:keys [offset size]}]
  (let [params (cond-> {:code code}
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/group-users) {:params params})))

(defn- get-all-by
  [conn func query-key response-key values]
  (go-loop [[parted :as rests] (partition-all 100 values)
            ret []]
    (if (empty? parted)
      (t/->KintoneResponse {response-key ret} nil)
      (let [res (<! (func conn {query-key parted}))]
        (if (:err res)
          res
          (recur (rest rests) (concat ret (-> res :res response-key))))))))

(defn- get-all
  [conn func response-key]
  (let [max-size 100]
    (go-loop [ret []]
      (let [res (<! (func conn {:offset (count ret)
                                :size max-size}))]
        (if (:err res)
          res
          (let [users (-> res :res response-key)
                ret (concat ret users)]
            (if (< (count users) max-size)
              (t/->KintoneResponse {response-key ret} nil)
              (recur ret))))))))

(defn get-all-users
  "Gets information of all users.

  opts - If :ids and :codes are both set, an error will return. Only one can be set.

    :ids - A list of User IDs.
           sequence of integer

    :codes - A list of User Codes (log-in names).
             sequence of string"
  [conn {:keys [ids codes]}]
  (cond
    (some? ids) (get-all-by conn get-users :ids :users ids)
    (some? codes) (get-all-by conn get-users :codes :users codes)
    :else (get-all conn get-users :users)))

(defn get-all-organizations
  "Gets information of all organizations.

  opts - If :ids and :codes are both set, an error will return. Only one can be set.

    :ids - A list of User IDs.
           sequence of integer

    :codes - A list of User Codes (log-in names).
             sequence of string"
  [conn {:keys [ids codes]}]
  (cond
    (some? ids) (get-all-by conn get-organizations :ids :organizations ids)
    (some? codes) (get-all-by conn get-organizations :codes :organizations codes)
    :else (get-all conn get-organizations :organizations)))

(defn get-all-groups
  "Gets information of all groups.

  opts - If :ids and :codes are both set, an error will return. Only one can be set.

    :ids - A list of User IDs.
           sequence of integer

    :codes - A list of User Codes (log-in names).
             sequence of string"
  [conn {:keys [ids codes]}]
  (cond
    (some? ids) (get-all-by conn get-groups :ids :groups ids)
    (some? codes) (get-all-by conn get-groups :codes :groups codes)
    :else (get-all conn get-groups :groups)))

(defn get-all-organization-users
  "Gets information of all users that belong to the organization. Each of userTitles may have \"title\" of the job

  code - The organization code.
         string"
  [conn code]
  (get-all conn #(get-organization-users %1 code %2) :userTitles))

(defn get-all-group-users
  "Gets information of all Users that belong to a Group.

  code - The group code.
         string"
  [conn code]
  (get-all conn #(get-group-users %1 code %2) :users))
