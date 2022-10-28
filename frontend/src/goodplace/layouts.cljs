(ns goodplace.layouts
  )

(defnc layout
  [{:keys [children]}]
  (let [pageData (usePage)]
    ($ Flex {:direction "column"
             :justify "center"
             :align "center"
             :width "100%"}
       ($ Flex {:height 12
                :justify "space-between"
                :align "center"
                :width "100%"}
          ($ Box {:py 2
                  :px 4}
             ($ Text "MaybeLogo"))
          ($ Box {:py 2
                  :px 4}
             ($ Text "MaybeLogin")))
       ($ Box children))))
