(ns ^{:doc "Distribute a release to GitHub."
      :author "Paul Landes"}
    zensols.ghrelease.dist
  (:import (org.kohsuke.github GitHub))
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:require [zensols.actioncli.log4j2 :as lu]
            [zensols.actioncli.dynamic :refer (defa-)]
            [zensols.actioncli.parse :refer (with-exception)])
  (:require [zensols.ghrelease.changelog :as cl]))

(defa- connection-inst)

(defn- connection
  "Create a connection to GitHub"
  []
  (swap! connection-inst #(or % (GitHub/connect))))

(defn- parse-tag [tag]
  (second (re-find #"^v?([0-9.]+)$" tag)))

(defn- squash-version
  "Map a version string to a number with an order based on each major, minor
  and debug etc versions of the version.  This is used
  by [[sort-version-tags]]."
  [version-string]
  (->> version-string
       parse-tag
       (#(s/split % #"[.]"))
       (map read-string)
       ((fn [vers]
          (map (fn [order ver]
                 (->> (repeat order 10)
                      (reduce *)
                      (* ver)))
               (-> vers count range reverse)
               vers)))
       (reduce +)))

(defn sort-version-tags
  "Sort version formats.  They have the form:
  `v?[0-9.]+` (i.e. v0.0.1)."
  [tags]
  (sort (fn [a b]
          (compare (squash-version a)
                   (squash-version b)))
        tags))

(defn repository
  "Return a repository by name, where name is <github user>/<repo name>."
  [repo-name]
  (log/debugf "looking up repo: %s" repo-name)
  (try (-> (connection) (.getRepository repo-name))
       (catch ArrayIndexOutOfBoundsException e
         (-> (format "repository not found: %s" repo-name)
             (ex-info {:repo-name repo-name})
             throw))))

(defn latest-tag
  "Find the latest (highest) for repository **repo**.
  See [[repository]]."
  [repo]
  (->> repo .listTags .iterator
       iterator-seq (map #(.getName %))
       (filter #(parse-tag %))
       sort-version-tags last))

(defn release
  "Look up release in repository **repo** with name (usually tag) **name**.
  See [[repository]]."
  [repo name]
  (log/debugf "looking up release %s in %s" name (.getName repo))
  (->> repo
       .listReleases
       .iterator iterator-seq
       (filter #(= name (.getName %)))
       first))

(defn- changelog-description-github
  [repo changelog-key & {:keys [changelog-file-name]
                         :or {changelog-file-name "CHANGELOG.md"}}]
 (try
   (-> repo
       (.getFileContent changelog-file-name)
       .read
       (cl/format-changelog-entry changelog-key))
   (catch java.io.FileNotFoundException e
     nil)))

(defn build-release
  "Build a release from repository **repo** for tag **tag**.

Keys
----
* **:name** the name of the release to create; defaults to **tag**
* **:changelog-resource** the CHANGELOG.md file to pull the description per **tag**
* **:description** the description (in markup) for the release
* **:prerelease?** Non-nil/false if this is a pre-release.

  See [[repository]]."
  [repo tag & {:keys [name changelog-resource description prerelease?]}]
  (log/infof "building release %s for %s" tag (.getName repo))
  (let [changelog-key (parse-tag tag)
        description (or (and changelog-resource
                             (cl/format-changelog-entry changelog-resource
                                                        changelog-key))
                        (changelog-description-github repo changelog-key)
                        description)
        builder (.createRelease repo tag)]
    (if name (.name builder name))
    (if description (.body builder description))
    (if prerelease? (.prerelease builder prerelease?))
    (.create builder)))

(defn upload-asset
  "Upload an asset to a release, which was created with [[release]]."
  [release file]
  (log/infof "uploading asset %s to %s" file (.getName release))
  (.uploadAsset release file "application/octet-stream"))

(defn- delete-release [repo release-name]
  (let [release (release repo release-name)]
    (if-not release
      (throw (ex-info (format "No such release %s found in repo %s"
                              release-name (.getName repo))
                      {:repo repo
                       :release-name release-name})))
    (.delete release)
    (log/infof "release %s in %s deleted" release-name (.getName repo))))

(defn- distribute-release
  [repo-name asset-files
   & {:keys [delete? tag name changelog-resource description prerelease?]
      :or {delete? true}}]
  (log/infof "creatig distribution release for %s" repo-name)
  (let [repo (repository repo-name)
        tag (or tag (latest-tag repo))
        name (or name tag)
        release (release repo name)
        release (cond (and (not delete?) release)
                      (throw (ex-info (format "Release %s already exists"
                                              repo-name)
                                      {:repo-name repo-name
                                       :name name
                                       :tag tag}))
                      (and delete? release)
                      (do (delete-release repo name) nil)
                      true release)
        release (or release
                    (build-release repo name
                                   :name tag
                                   :changelog-resource changelog-resource
                                   :description description
                                   :prerelease? prerelease?))]
    (doseq [asset-file asset-files]
      (upload-asset release asset-file))
    (log/infof "distribution complete: %s/%s" repo-name name)))

(defn- args-to-files [args]
  (->> args
       (map (fn [arg]
              (let [file (io/file arg)]
                (if (or (.isDirectory file) (not (.exists file)))
                  (-> (format "File does not exist: %s" file)
                      (ex-info {:file file})
                      throw))
                file)))))

(def dist-command
  "CLI command to invoke hello world"
  {:description "create a GitHub release"
   :options
   [(lu/log-level-set-option)
    ["-r" "--repo" "the repository identifier (ex: plandes/clj-ghrelease)"
     :required "<user/repo name>"
     :missing "Missing required -r"]
    ["-t" "--tag" "the version format tag of the release (ex: v0.0.1)"
     :required "<v?[0-9.]+|latest>"
     :default "latest"
     :validate [parse-tag "Tag is not valid"]]
    ["-n" "--name" "the optional name of the release, which defaults to the latest tag"
     :required "<name>"]
    [nil "--nodelete" "don't delete current release if it exists already"]
    ["-d" "--description" "the optional description of the release"
     :required "<description>"]
    ["-c" "--changelog" "description is parsed from changelog (default to repo)"
     :required "<CHANGELOG.md>"
     :validate [#(.exists %) "No such file"]
     :parse-fn #(io/file %)]
    ["-p" "--prerelease" "indicate this is a pre-release"]]
   :app (fn [{:keys [repo tag name nodelete description prerelease changelog]
              :as opts}
             & args]
          (with-exception
            (let [tag (if (= "latest" tag) nil tag)
                  files (args-to-files args)]
              (if (empty? files)
                (throw (ex-info "No files given" {})))
              (distribute-release repo files
                                  :tag tag
                                  :name name
                                  :delete? (not nodelete)
                                  :changelog-resource changelog
                                  :description description
                                  :prerelease? prerelease))))})
