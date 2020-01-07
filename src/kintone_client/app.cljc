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
  "Gets general information of multiple Apps, including the name, description, related Space, creator and updater information.

  opts

    :offset - The number of retrievals that will be skipped.
              The default value is 0 if this is not assigned.
              integer

    :limit - The number of Apps to retrieve.
             The default value is 100 if this is not assigned.
             integer

    :codes - The App Code.
             sequence of string

    :name - The App Name.
            string

    :ids - The App IDs.
           sequence of integer

    :space-ids - The space id where the app resides.
                 sequence of integer"
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
  "Gets form layout of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :preview? - Gets from pre-live settings."
  [conn app {:keys [preview?]}]
  (let [url (pt/-url conn (if preview?
                            path/preview-form-layout
                            path/form-layout))]
    (pt/-get conn url {:params {:app app}})))

(defn update-form-layout
  "Updates the field layout info of a form in an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID
        integer

  layout - A list of field layouts for each row.
           See API reference regarding form layout format.
           sequence of map"
  [conn app layout {:keys [revision]}]
  (let [params (cond-> {:app app :layout layout}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-layout)]
    (pt/-put conn url {:params params})))

(defn get-form-fields
  "Gets the list of fields and field settings of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :lang - The localized language to retrieve the data.
            Support:
              default: default name
              ja: Japanese
              zh: Chinese
              en: English
              user: the language setting set on the user used for the authentication
            The default value is default if this is not assigned.
            string

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [lang preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if preview?
                            path/preview-form-fields
                            path/form-fields))]
    (pt/-get conn url {:params params})))

(defn add-form-fields
  "Adds fields to a form of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  fields - The field settings.
           See API reference regarding field format.
           map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app fields {:keys [revision]}]
  (let [params (cond-> {:app app :properties fields}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-post conn url {:params params})))

(defn update-form-fields
  "Updates the field settings of fields in a form of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  fields - The field settings.
           See API reference regarding record format.
           map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app fields {:keys [revision]}]
  (let [params (cond-> {:app app :properties fields}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-put conn url {:params params})))

(defn delete-form-fields
  "Deletes fields from a form of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  codes - The list of field codes of the fields to delete.
          Up to 100 field codes can be specified.
          sequence

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app codes {:keys [revision]}]
  (let [params (cond-> {:app app :fields codes}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-form-fields)]
    (pt/-delete conn url {:params params})))

(defn add-preview-app
  "Creates a preview App.
  The `deploy-app-settings` must be used on the created preview App for the App to become live.
  API Tokens cannot be used with this API.

  opts

    :name - The App name. (Required)
            string

    :space - The Space ID of where the App will be created.
             integer

    :thread - The Thread ID of the thread in the Space where the App will be created.
              integer"
  [conn {:keys [name space thread]}]
  (let [params (cond-> {:name name}
                 (some? space) (assoc :space space)
                 (some? thread) (assoc :thread thread))
        url (pt/-url conn path/preview-app)]
    (pt/-post conn url {:params params})))

(defn deploy-app-settings
  "Updates the settings of a pre-live App to the live App.
  API Tokens cannot be used with this API.

  apps - The list of Apps to deploy the pre-live settings to the live Apps.
         sequence of integer

  opts

    :revert - Specify true to cancel all changes made to the pre-live settings.
              boolean"
  [conn apps {:keys [revert]}]
  (let [params (cond-> {:apps apps}
                 (some? revert) (assoc :revert revert))
        url (pt/-url conn path/preview-deploy)]
    (pt/-post conn url {:params params})))

(defn get-app-deploy-status
  "Gets the deployment status of the App settings for multiple Apps.
  API Tokens cannot be used with this API.

  apps - Sequence of kintone app ID.
         sequence of integer"
  [conn apps]
  (let [params {:apps apps}
        url (pt/-url conn path/preview-deploy)]
    (pt/-get conn url {:params params})))

(defn get-views
  "Gets the View settings of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :lang - The localized language to retrieve the data.
            Support:
              default: default name
              ja: Japanese
              zh: Chinese
              en: English
              user: the language setting set on the user used for the authentication
            The default value is default if this is not assigned.
            string

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [lang preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if preview?
                            path/preview-views
                            path/views))]
    (pt/-get conn url {:params params})))

(defn update-views
  "Updates the View settings of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.

  views - The view settings.
          See API reference regarding record format.
          map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app views {:keys [revision]}]
  (let [params (cond-> {:app app :views views}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-views)]
    (pt/-put conn url {:params params})))

(defn get-general-settings
  "Gets the description, name, icon, revision and color theme of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :lang - The localized language to retrieve the data.
            Support:
              default: default name
              ja: Japanese
              zh: Chinese
              en: English
              user: the language setting set on the user used for the authentication
            The default value is default if this is not assigned.
            string

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [lang preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if preview?
                            path/preview-settings
                            path/settings))]
    (pt/-get conn url {:params params})))

(defn update-general-settings
  "Updates the description, name, icon, revision and color theme of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.

  settings - The general settings.
             Parameters that are ignored will not be updated.
             See API reference regarding general settings format.
             map

    :name - The App name.
            string

    :icon - The app icon.
            map

      :type - The icon type. \"FILE\" or \"PRESET\"
              string

      :key - The key identifier of the icon.
             string

      :file - The key identifier of the icon.
              map

        :fileKey - The file key of the icon.
                   string

    :theme - The Color theme.

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app settings {:keys [revision]}]
  (let [params (cond-> (merge {:app app} settings)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-settings)]
    (pt/-put conn url {:params params})))

(defn get-status
  "Gets the process management settings of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :lang - The localized language to retrieve the data.
            Support:
              default: default name
              ja: Japanese
              zh: Chinese
              en: English
              user: the language setting set on the user used for the authentication
            The default value is default if this is not assigned.
            string

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [lang preview?]}]
  (let [params (cond-> {:app app}
                 (some? lang) (assoc :lang lang))
        url (pt/-url conn (if preview?
                            path/preview-status
                            path/status))]
    (pt/-get conn url {:params params})))

(defn update-status
  "Updates the process management settings of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone App ID.

  status - The process management setting.
           See API reference regarding process management format.
           map

    :enable - The on/off settings of the process management settings.
              string

    :states - The process management statuses.
              map

    :actions - The actions.
               sequence of map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app status {:keys [revision]}]
  (let [params (cond-> (merge {:app app} status)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-status)]
    (pt/-put conn url {:params params})))

(defn get-customize
  "Gets the JavaScript and CSS Customization settings of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if preview?
                            path/preview-customize
                            path/customize))]
    (pt/-get conn url {:params params})))

(defn update-customize
  "Updates the JavaScript and CSS Customization settings of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  customize - Customize setting.
              See API reference regarding customize format.
              map

    :desktop - The setting of JavaScript and CSS files for the desktop.
               map

    :mobile - The setting of JavaScript and CSS files for the desktop.
              map

    :scope - The scope of customization.
             string

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app customize {:keys [revision]}]
  (let [params (cond-> (merge {:app app} customize)
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-customize)]
    (pt/-put conn url {:params params})))

(defn get-acl
  "Gets the App permissions of an app.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

    :preview? - Gets from pre-live settings.
                   boolean"
  [conn app {:keys [preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if preview?
                            path/preview-acl
                            path/acl))]
    (pt/-get conn url {:params params})))

(defn update-acl
  "Updates the App permissions of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.

  rights - The permission settings.
           See API reference regarding permission format.
           sequence of map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-acl)]
    (pt/-put conn url {:params params})))

(defn get-field-acl
  "Gets the Field permission settings of an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.
        integer

  opts

  :preview? - Gets from pre-live settings.
                 boolean"
  [conn app {:keys [preview?]}]
  (let [params {:app app}
        url (pt/-url conn (if preview?
                            path/preview-field-acl
                            path/field-acl))]
    (pt/-get conn url {:params params})))

(defn update-field-acl
  "Updates the Field permission settings of an App.
  After using this API, use the `deploy-app-settings` to deploy the settings to the live App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.

  rights - The field permission settings.
           See API reference regarding field permission format.
           sequence of map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/preview-field-acl)]
    (pt/-put conn url {:params params})))

(defn update-live-field-acl
  "Updates the Field permission settings of LIVE ENVIRONMENT an App.
  API Tokens cannot be used with this API.

  app - The kintone app ID.

  rights - The field permission settings.
           See API reference regarding field permission format.
           sequence of map

  opts

    :revision - Specify the revision number of the settings that will be deployed.
                integer"
  [conn app rights {:keys [revision]}]
  (let [params (cond-> {:app app :rights rights}
                 (some? revision) (assoc :revision revision))
        url (pt/-url conn path/field-acl)]
    (pt/-put conn url {:params params})))
