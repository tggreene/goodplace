(ns goodplace.handlers.notes
  (:require [inertia.middleware :as inertia]
            [goodplace.models.notes :as model]
            [goodplace.handlers.common :as common]
            [goodplace.shared.routes :as routes]
            [ring.util.response :as response]))

(defn list-notes
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          notes (model/list-user-notes db id)]
      (inertia/render :notes {:notes notes}))))

(defn view-note
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          note-id (get-in request [:path-params :note-id])
          note (model/get-note-by-id db note-id)]
      (cond
        (= id (:user note)) (inertia/render :view-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-get
  [{:keys [db]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          note-id (get-in request [:path-params :note-id])
          note (model/get-note-by-id db note-id)]
      (cond
        (= id (:user note)) (inertia/render :edit-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-post
  [{:keys [db]}]
  (fn [request]
    (let [note (:body-params request)
          _ (model/update-note! db note)
          new-note (model/get-note-by-id db (:id note))]
      (inertia/render :view-note {:note new-note}))))

(defn create-note-post
  [{:keys [db]}]
  (fn [request]
    (let [user (common/get-user request)
          note (:body-params request)
          {:keys [id] :as new-note} (model/create-note! db (assoc note :user (:id user)))]
      (response/redirect (routes/get-route-path :view-note {:note-id id}) :see-other))))

(defn delete-note
  [{:keys [db]}]
  (fn [request]
    (let [user (common/get-user request)
          note-id (get-in request [:path-params :note-id])]
      (model/soft-delete-note! db note-id)
      (response/redirect (routes/get-route-path :notes) :see-other))))
