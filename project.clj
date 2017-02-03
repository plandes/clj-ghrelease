(defproject com.zensols.tools/ghrelease "0.1.0-SNAPSHOT"
  :description "Upload distribution binaries to Github"
  :url "https://github.com/plandes/clj-ghrelease"
  :license {:name "Apache License version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :plugins [[lein-codox "0.10.1"]
            [org.clojars.cvillecsteele/lein-git-version "1.2.7"]]
  :codox {:metadata {:doc/format :markdown}
          :project {:name "clj-ghrelease"}
          :output-path "target/doc/codox"
          :source-uri "https://github.com/plandes/clj-ghrelease/blob/v{version}/{filepath}#L{line}"}
  :git-version {:root-ns "zensols.ghrelease"
                :path "src/clojure/zensols/ghrelease"
                :version-cmd "git describe --match v*.* --abbrev=4 --dirty=-dirty"}
  :source-paths ["src/clojure"]
  :java-source-paths ["src/java"]
  :javac-options ["-Xlint:unchecked"]
  :jar-exclusions [#".gitignore"]
  :dependencies [[org.clojure/clojure "1.8.0"]

                 ;; logging for core
                 [org.apache.logging.log4j/log4j-core "2.7"]

                 ;; java gh api
                 [org.kohsuke/github-api "1.84"]

                 ;; command line
                 [com.zensols.tools/actioncli "0.0.15"]]
  :pom-plugins [[org.codehaus.mojo/appassembler-maven-plugin "1.6"
                 {:configuration ([:programs
                                   [:program
                                    ([:mainClass "zensols.ghrelease.core"]
                                     [:id "ghrelease"])]]
                                  [:environmentSetupFileName "setupenv"])}]]
  :profiles {:uberjar {:aot [zensols.ghrelease.core]}
             :appassem {:aot :all}
             :dev
             {:jvm-opts ["-Dlog4j.configurationFile=test-resources/log4j2.xml" "-Xms4g" "-Xmx12g" "-XX:+UseConcMarkSweepGC"]
              :exclusions [org.slf4j/slf4j-log4j12
                           log4j/log4j
                           ch.qos.logback/logback-classic]
              :dependencies [[org.apache.logging.log4j/log4j-slf4j-impl "2.7"]
                             [org.apache.logging.log4j/log4j-1.2-api "2.7"]
                             [org.apache.logging.log4j/log4j-jcl "2.7"]
                             [org.clojure/math.combinatorics "0.1.4"]
                             [com.zensols/clj-append "1.0.5"]]}}
  :main zensols.ghrelease.core)
