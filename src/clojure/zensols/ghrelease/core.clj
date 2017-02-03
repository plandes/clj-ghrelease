(ns zensols.ghrelease.core
  (:require [zensols.actioncli.log4j2 :as lu]
            [zensols.actioncli.parse :as cli])
  (:require [zensols.ghrelease.version :as ver])
  (:gen-class :main true))

(defn- version-info []
  (println (format "%s (%s)" ver/version ver/gitref)))

(defn- create-action-context []
  (cli/single-action-context
   '(zensols.ghrelease.dist dist-command)
   :version-option (cli/version-option version-info)
   :usage-format-fn (->> "usage: %s%s [options] <file1> [file2]..."
                         cli/create-default-usage-format)))

(defn -main [& args]
  (lu/configure "ghrelease-log4j2.xml")
  (cli/set-program-name "ghrelease")
  (-> (create-action-context)
      (cli/process-arguments args)))
