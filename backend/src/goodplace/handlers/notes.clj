(ns goodplace.handlers.notes
  (:require [inertia.middleware :as inertia]
            [goodplace.models.notes :as model]
            [goodplace.handlers.common :as common]
            [goodplace.shared.routes :as routes]
            [ring.util.response :as response]))

(defn list-notes
  [{:keys [postgres]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          notes (model/list-user-notes postgres id)]
      (inertia/render :notes {:notes notes}))))

(defn view-note
  [{:keys [postgres]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          note-id (get-in request [:path-params :note-id])
          note (model/get-note-by-id postgres note-id)]
      (cond
        (= id (:user_id note)) (inertia/render :view-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-get
  [{:keys [postgres]}]
  (fn [request]
    (let [{:keys [id] :as user} (common/get-user request)
          note-id (get-in request [:path-params :note-id])
          note (model/get-note-by-id postgres note-id)]
      (cond
        (= id (:user_id note)) (inertia/render :edit-note {:note note})
        note (inertia/render :home {:errors ["Not permitted to view note"]
                                    :redirect (routes/get-route-path :notes)})
        :else (inertia/render :home {:errors ["Note not found"]
                                     :redirect (routes/get-route-path :notes)})))))

(defn edit-note-post
  [{:keys [postgres]}]
  (fn [request]
    (let [note-id (get-in request [:path-params :note-id])
          note (:body-params request)]
      (model/update-note! postgres (assoc note :id note-id))
      (response/redirect
       (routes/get-route-path :view-note {:note-id note-id})
       :see-other))))

(defn create-note-post
  [{:keys [postgres]}]
  (fn [request]
    (let [user (common/get-user request)
          note (:body-params request)
          {:keys [id] :as new-note}
          (model/create-note! postgres
                              (assoc note :user (:id user)))]
      (response/redirect
       (routes/get-route-path :view-note {:note-id id})
       :see-other))))

(defn delete-note
  [{:keys [postgres]}]
  (fn [request]
    (let [user (common/get-user request)
          note-id (get-in request [:path-params :note-id])]
      (model/soft-delete-note! postgres note-id)
      (response/redirect (routes/get-route-path :notes) :see-other))))
