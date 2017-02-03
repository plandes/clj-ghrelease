(ns ^{:doc "Parse a CHANGELOG.md file.  The file format *must* follow the [keep
a CHANGELOG](http://keepachangelog.com/)."
      :author "Paul Landes"}
    zensols.ghrelease.changelog
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:require [zensols.actioncli.dynamic :refer (defa-)]))

(def ^:dynamic *changelog-head-regexp*
  #"^##\s*\[([\w\s.]+)\]\s*(?:[-]\s*(\d{4}-\d{2}-\d{2})\s*)?$")

(defn parse-changelog-entries
  "Parse entries from a CHANGELOG.md formatted file (see namespace docs).
  Parameter **changelog-resource** can be anything
  that [[clojure.java.io/reader]] can parse."
  [changelog-resource]
  (log/debugf "parisng changelog resource: %s" changelog-resource)
  (with-open [reader (io/reader changelog-resource)]
    (->> reader
         line-seq
         (reduce (fn [{:keys [lines entries last-header]} line]
                   (let [[_ tag date] (re-find *changelog-head-regexp* line)
                         header (if tag {:tag tag :date date})
                         entries (if header
                                   (conj entries
                                         (assoc last-header :lines lines))
                                   entries)
                         last-header (or header last-header)
                         lines (if-not header
                                 (conj lines line)
                                 [])]
                     {:lines lines
                      :entries entries
                      :last-header last-header}))
                 {:lines []
                  :entries []})
         ((fn [{:keys [entries lines last-header]}]
            (concat (rest entries) [(assoc last-header :lines lines)])))
         (map (fn [{:keys [tag] :as entry}]
                {tag (dissoc entry :tag)}))
         (apply merge))))

(defn format-changelog-entry
  "Parse a CHANGELOG.md and return the **tag** entry if it exists or nil
  otherwise.
  See [[parse-changelog-entries]]."
  [changelog-resource tag]
  (let [entry (-> (parse-changelog-entries changelog-resource)
                  (get tag))]
    (if entry
      (->> (:lines entry)
           (s/join \newline)
           s/trim))))
