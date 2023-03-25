#!/bin/bash

#
# Copyright (C) 2023 Rick Busarow
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# exit when any command fails
set -e

VERSION_TOML=gradle/libs.versions.toml

step=0

# usage - `progress "my status update"`
function progress() {
  step=$((step + 1))
  echo
  echo "$step -- $1"
  echo
}

# usage - `maybeCommit "updated something"`
function maybeCommit() {
  # add any newly-created files
  git add -A

  if (git diff --quiet && git diff --staged --quiet); then
    # there are no changes
    echo no changes -- nothing to commit
  else
    git commit -am "$1"
  fi
}

# Ensure that there are no changes which aren't committed
if ! (git diff --quiet); then
  # there are changes
  echo
  echo Commit or revert the existing changes before running the release script.
  echo
  exit 1
fi

# keep track of the last executed command
# shellcheck disable=SC2154
trap 'last_command=$current_command; current_command=$BASH_COMMAND' DEBUG
# echo an error message before exiting
# shellcheck disable=SC2154
trap 'echo "\"${last_command}\" command filed with exit code $?."' EXIT

# Don't release a =SNAPSHOT
progress "check version isn't a -SNAPSHOT"
./gradlew checkVersionIsNotSnapshot

# Matches the '## <some version> (unreleased)' string near the top of the CHANGELOG file.
# Note that this is awk, so '$2' isn't a capture group -- it's the second "field" (the version).
# The line is delimited by whitespaces.
NEXT_VERSION=$(awk '/.*\(unreleased)/ { print $2}' CHANGELOG.md | sed 's/\"//g')

function parseVersionAndSyncDocs() {

  # Parse the 'docusync' version from libs.versions.toml
  # Removes the double quotes around the raw string value
  VERSION_NAME=$(awk -F ' *= *' '$1=="docusync"{print $2}' $VERSION_TOML | sed 's/\"//g')

  # Add `@since ____` tags to any new KDoc
  progress "Add \`@since ____\` tags to any new KDoc"
  ./gradlew ktlintFormat
  maybeCommit "add @since tags to new KDoc for $VERSION_NAME"

  # update the version references in docs before versioning them
  progress "Update docs versions"
  ./gradlew spotlessApply
  maybeCommit "update version references in docs to $VERSION_NAME"

  # Copy the CHANGELOG from project root to the website dir and update its formatting
  progress "update the website changelog"
  ./gradlew updateWebsiteChangelog

  # This adds a new version for the Dokka api docs and the markdown /docs docs.
  progress "Create new website docs version"
  ./gradlew versionDocs
  maybeCommit "create new docs version for $VERSION_NAME"
}

# update all versions/docs for the release version
parseVersionAndSyncDocs

# Generate all api docs and make sure they're in ./dokka-archive/
# Then ensure that all the new versioned api docs are tracked by Git
progress "generate and copy Dokka api docs"
./gradlew dokkaHtmlMultiModule syncDokkaToDokkaArchive

# add the new version of Dokka archive to git and commit those files as their own commit.
maybeCommit "add Dokka docs for ${VERSION_NAME} to the dokka-archive"

# One last chance to catch any bugs
progress "run the check task"
./gradlew check --no-configuration-cache

progress "Publish Maven release"
./gradlew publish --no-configuration-cache

# Publish to Gradle Plugin Portal
# progress "Publish to Gradle Plugin Portal"
./gradlew publishPlugins --no-configuration-cache

# Create the "Releasing ______" commit and a new tag for the current `VERSION_NAME`
progress "commit the release and tag"
git commit --allow-empty -am "Releasing ${VERSION_NAME}"
git tag "${VERSION_NAME}"
git push --tags

progress "create the release on GitHub"
./gradlew githubRelease

progress "update the dev version to ${NEXT_VERSION}"
OLD="(^ *docusync *= *)\"${VERSION_NAME}\""
NEW="\$1\"${NEXT_VERSION}\""
# Write the new -SNAPSHOT version to the versions toml file
perl -pi -e "s/$OLD/$NEW/" $VERSION_TOML
git commit -am "update dev version to ${NEXT_VERSION}"

# update all versions/docs for the next version
parseVersionAndSyncDocs

echo
echo ' ___ _   _  ___ ___ ___ ___ ___ '
echo '/ __| | | |/ __/ __| __/ __/ __|'
echo '\__ \ |_| | (_| (__| _|\__ \__ \'
echo '|___/\___/ \___\___|___|___/___/'
echo
echo
echo The release is done and a new docs version has been created for Docusaurus.
echo
echo Next, just create a PR to merge all these distinct commits into the remote main branch.
echo
