(ns kintone-client.user
  (:require [kintone-client.constant.path.user :as path]
            [kintone-client.protocols :as pt]))

(defn get-users
  "Gets information of users.

  opts

    :ids - A list of User IDs.
           sequence of integer

    :codes - A list of User Codes (log-in names).
             sequence of string

    :offset - The offset.
              integer

    :size - The maximum number of User information to get.
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

    [] :currentCode - The current User Code (log-in name).
                      string

    [] :newCode - The new User Code (log-in name).
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
              integer

    :size - The maximum number of Organization information to get.
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

(defn update-org-codes
  "Updates organization codes of organizations.

  codes - sequence of map

    [] :currentCode - The current Organization Code.
                      string

    [] :newCode - The new Organization Code.
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
              integer

    :size - The maximum number of Group information to get.
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
  "Gets information of Users that belong to a organization, including their Job title.

  code - The organization code.
         string

  opts

    :offset - The offset.
              integer

    :size - The maximum number of Organization information to get.
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
              integer

    :size - The maximum number of Group information to get.
            integer"
  [conn code {:keys [offset size]}]
  (let [params (cond-> {:code code}
                 (some? offset) (assoc :offset offset)
                 (some? size) (assoc :size size))]
    (pt/-get conn (pt/-user-api-url conn path/group-users) {:params params})))

