[![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.ktrules/ktrules?style=flat-square)](https://search.maven.org/search?q=com.rickbusarow.ktrules)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.rickbusarow.ktrules/ktrules?label=snapshots&server=https%3A%2F%2Foss.sonatype.org&style=flat-square)](https://oss.sonatype.org/#nexus-search;quick~com.rickbusarow.ktrules)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

# ktrules

**ktrules** is a collection of KtLint rules designed to improve the quality, consistency, and
readability of Kotlin code.

The rules in ktrules cover a wide range of coding styles and best practices, from formatting and
indentation to naming conventions and documentation. Some of the specific areas covered by the rules
include:

This is a sentence before a bulleted list. It does not have a space after it.

- Main point
- another main point
  - a sub point
  - another one

## Rules

<!--docusync editorconfig-sample-->

```editorconfig
# KtLint specific settings
# noinspection EditorConfigKeyCorrectness
[{*.kt,*.kts}]
ktlint_kt-rules_kdoc-leading-asterisk = enabled
ktlint_kt-rules_kdoc-wrapping = enabled
ktlint_kt-rules_no-duplicate-copyright-header = enabled
ktlint_kt-rules_no-leading-blank-lines = enabled
ktlint_kt-rules_no-since-in-kdoc = enabled
ktlint_kt-rules_no-space-in-annotation-with-target = enabled
ktlint_kt-rules_no-trailing-space-in-raw-string-literal = enabled
ktlint_kt-rules_no-useless-constructor-keyword = enabled

ktlint_kt-rules_project_version = 1.0.0
ktlint_kt-rules_wrapping_style = equal

[{*.kt,*.kts}]
# actual kotlin settings go here
```

<!--/docusync-->

|                  Rule                  |                                                                Description                                                                 |
| :------------------------------------: | :----------------------------------------------------------------------------------------------------------------------------------------: |
|        KDocLeadingAsteriskRule         |                                 ensures that the leading asterisk in a KDoc comment is followed by a space                                 |
|            KDocWrappingRule            |                                    ensures consistent wrapping of KDoc comments to improve readability                                     |
|     NoDuplicateCopyrightHeaderRule     |                                            ensures that each file has only one copyright header                                            |
|        NoLeadingBlankLinesRule         |                                           ensures that there are no leading blank lines in files                                           |
|           NoSinceInKDocRule            |                                            ensures that there is no @since tag in KDoc comments                                            |
|    NoSpaceInTargetedAnnotationRule     | ensures that targeted annotations (annotations with a target specifier such as @get:, @set:, etc.) have no space before or after the colon |
| NoTrailingSpacesInRawStringLiteralRule |                                      ensures that there are no trailing spaces in raw string literals                                      |
|    NoUselessConstructorKeywordRule     |                                    removes the unnecessary constructor keyword from class constructors                                     |

## Gradle Usage

To use ktrules in your project, follow the documentation for your existing integration solution using
third-party extensions. Typically, your plugin or custom tasks will have a Gradle configuration
named `ktlint`. In this case, you would add the `ktrules` dependency to your project's build file like:

<!--docusync maven-artifact:1-->

```kotlin
// build.gradle.kts
dependencies {
  ktlint("com.rickbusarow.ktrules:ktrules:1.0.1")
}
```

<!--/docusync-->

## `@since` tags

The `NoSinceInKDocRule` must know your project's current version in order to automatically add tags. It
can read that version from an editorconfig property named `project_version` or a System property
named `ktrules.project_version`.

This can be set inside your KtLint task's configuration. For example, if the version is defined
inside `libs.versions.toml` it would look like this:

```kotlin
tasks.withType<ConfigurableKtLintTask> {
  // set the project's version as a System property so that it can be read by NoSinceInKDocRule
  System.getProperties()
    .setIfAbsent("ktrules.project_version") { libs.versions.currentVersion.get() }
}

fun java.util.Properties.setIfAbsent(name: String, value: () -> String) {
  if (getProperty(name) == null) {
    setProperty(name, value())
  }
}
```

## Contributing

If you'd like to contribute to ktrules, please submit a pull request with your changes. Bug reports or
new rule requests are also welcome in the issue tracker.

### License

```text
Copyright (C) 2023 Rick Busarow
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
     https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
