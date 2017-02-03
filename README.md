# GitHub Automated Releases

[![Travis CI Build Status][travis-badge]][travis-link]

  [travis-link]: https://travis-ci.org/plandes/clj-ghrelease
  [travis-badge]: https://travis-ci.org/plandes/clj-ghrelease.svg?branch=master

Create and upload distribution binaries as
a [GitHub release](https://help.github.com/articles/about-releases/).  This
software is meant to replace
the [manual](https://help.github.com/articles/creating-releases/) process.


## Contents

* [Obtaining](#obtaining)
* [Usage](#usage)
  - [Changelog](#changelog)
* [Building](#building)
* [Use as a Clojure Library](#use-as-a-clojure-library)
  - [Obtaining](#obtaining)
* [Changelog](#changelog)
* [Documentation](#documentation)
* [License](#license)


## Obtaining

The latest release binaries are
available [here](https://github.com/plandes/clj-ghrelease/releases/latest).

## Usage

This is a command line application that has the following usage (given with `-h`):
```sql
usage: ghrelease [options] <file1> [file2]...
create a GitHub release
  -l, --level <log level>          INFO    Log level to set in the Log4J2 system.
  -r, --repo <user/repo name>              the repository identifier (ex: plandes/clj-ghrelease)
  -t, --tag <v?[0-9.]+|latest>     latest  the version format tag of the release (ex: v0.0.1)
  -n, --name <name>                        the optional name of the release, which defaults to the latest tag
      --nodelete                           don't delete current release if it exists already
  -d, --description <description>          the optional description of the release
  -c, --changelog <CHANGELOG.md>           description is parsed from changelog (default to repo)
  -p, --prerelease                         indicate this is a pre-release
```

The only argument required is the repository name.  The tag defaults to the
latest tag and the description defaults to the tag's version found in the
`CHANGELOG.md`.


### Changelog

This tool parses the `CHANGELOG.md` to create the description for the release.
The file format *must* follow
the [keep a CHANGELOG](http://keepachangelog.com/).  If no changelog file is
given on the command line it is taken from master HEAD ref content currently
committed to the repository (see [Changelog](#changelog)).  This can be
overridden by providing a `-d` option.


## Building

To build from source, do the folling:

- Install [Leiningen](http://leiningen.org) (this is just a script)
- Install [GNU make](https://www.gnu.org/software/make/)
- Install [Git](https://git-scm.com)
- Download the source: `git clone https://github.com/clj-nlp-feature && cd clj-nlp-feature`
- Download the make include files:
```bash
mkdir ../clj-zenbuild && wget -O - https://api.github.com/repos/plandes/clj-zenbuild/tarball | tar zxfv - -C ../clj-zenbuild --strip-components 1
```
- Compile: `make compile` do compile or `make install` to install in your local
  maven repo.


## Use as a Clojure Library

This software is written in Clojure and can be used in your own Clojure (or
Java) program.  For example, you could create
a [Leiningen plugin](https://nakkaya.com/2010/02/25/writing-leiningen-plugins-101/).


### Obtaining

In your `project.clj` file, add:

[![Clojars Project](https://clojars.org/com.zensols.tools/ghrelease/latest-version.svg)](https://clojars.org/com.zensols.tools/ghrelease/)


### Documentation

API [documentation](https://plandes.github.io/clj-ghrelease/codox/index.html).


## Changelog

An extensive changelog for *this* package is available [here](CHANGELOG.md).


## License

Copyright © 2017 Paul Landes

Apache License version 2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
