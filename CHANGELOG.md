# Changelog

## 1.1.4-SNAPSHOT (unreleased)

### Changed

- The editorconfig properties `ktlint_kt-rules_project_version` and `ktlint_kt-rules_wrapping_style`
  have been deprecated and replaced with `kt-rules_project_version` and `kt-rules_wrapping_style`
  respectively.  This is to avoid KtLint trying to parse them as RuleIds.


## [1.1.3] - 2023-06-29

The default artifact is now updated
to [KtLint 0.50.0](https://github.com/pinterest/ktlint/releases/tag/0.50.0).

Because 0.50.0 has a number of api-breaking changes again, the published artifacts have been split.

These artifacts all have the same rule logic, but use different KtLint versions.

| if you use KtLint... |         use the ktrules artifact:          |
| :------------------: | :----------------------------------------: |
|        0.47.x        | `com.rickbusarow.ktrules:ktrules-47:1.1.0` |
|        0.48.x        | `com.rickbusarow.ktrules:ktrules-48:1.1.0` |
|        0.49.x        | `com.rickbusarow.ktrules:ktrules-49:1.1.0` |
|        0.50.x        |  `com.rickbusarow.ktrules:ktrules:1.1.0`   |

**Full Changelog**: https://github.com/RBusarow/ktrules/compare/1.1.2...1.1.3

## [1.1.2] - 2023-06-07

### Fixed

- `kt-rules:no-since-in-kdoc` won't indent by an extra space while adding `@since` to a top-level KDoc
- `kt-rules:no-since-in-kdoc` won't add `@since null` to a KDoc if the current version property is `null`
- `kt-rules:kdoc-content-wrapping` won't delete KDoc ends (`*/`) from inside code blocks while wrapping
- `kt-rules:kdoc-tag-order` won't sort KDoc tags until after rules which add or change tags

**Full Changelog**: https://github.com/RBusarow/ktrules/compare/1.1.1...1.1.2

## [1.1.1] - 2023-04-29

### Fixed

- `kt-rules:kdoc-content-wrapping` won't wrap "unknown" tags into a preceding paragraph
- `kt-rules:kdoc-leading-asterisk` will now add a leading asterisk to a blank line in between two
  tags

## [1.1.0] - 2023-04-22

The default artifact is now updated
to [KtLint 0.49.0](https://github.com/pinterest/ktlint/releases/tag/0.49.0).

Because 0.49.0 has a large number of api-breaking changes, the published artifacts have been split.

These artifacts all have the same rule logic, but use different KtLint versions.

| if you use KtLint... |         use the ktrules artifact:          |
| :------------------: | :----------------------------------------: |
|        0.47.x        | `com.rickbusarow.ktrules:ktrules-47:1.1.0` |
|        0.48.x        | `com.rickbusarow.ktrules:ktrules-48:1.1.0` |
|        0.49.x        |  `com.rickbusarow.ktrules:ktrules:1.1.0`   |

## [1.0.9] - 2023-04-19

### Added

- `no-gradle-with-type-with-lambda` ensures calls to Gradle's `withType(...)`
  use `configureEach { }` for the lambda.

## [1.0.8] - 2023-04-18

### Fixed

- `kdoc-collapse` will no longer emit an error if a KDoc is already properly collapsed.

## [1.0.7] - 2023-04-18

### Fixed

- `kdoc-content-wrapping` will now add newlines around a wrapped kdoc if the original is a collapsed
  single-line comment

### Added

- `kdoc-blank-lines` ensures that there are no consecutive or extraneous blank lines within a KDoc
  comment's content.
- `kdoc-collapse` collapses short KDoc comments into a single line, like `/** my comment */`

### Changed

- The rule `KDocWrappingRule` (`kdoc-wrapping`) has been renamed
  to `KDocContentWrappingRule`/`kdoc-content-wrapping` in order to avoid a conflict with
  the [new experimental rule](https://pinterest.github.io/ktlint/rules/experimental/#kdoc-wrapping)
  in the KtLint library.

## [1.0.6] - 2023-04-07

### Fixed

- `kdoc-wrapping` will no longer add a new line at the end of a KDoc when wrapping the last tag.
- `no-duplicate-copyright-header` will now properly remove duplicate copyright headers from
  script (`.kts`) files

## [1.0.5] - 2023-04-06

### Fixed

- `kdoc-tag-order` will now properly sort `@param` and `@property` tags when the
  function/constructor has 10 or more parameters

### Added

- `kdoc-tag-param-or-property` ensures that KDoc comments use `@property` tags for properties (`val`
  or `var`) and `@param` tags for type parameters (like `T`) or non-property value parameters.

## [1.0.4] - 2023-04-05

### Fixed

- `kdoc-wrapping` will no longer group words inside bold/italic delimiters if there's a whitespace
  immediately inside either delimiter, like `__ this __`. Markdown does not render the text style if
  there's a space. Spaces in between words are still okay.

### Added

- `kdoc-indent-after-leading-asterisk` ensures that every leading asterisk in a KDoc default section
  is followed by at least one whitespace, and every leading asterisk after a KDoc tag is followed by
  at least three whitespaces.
- `kdoc-tag-order` ensures that KDoc tags are sorted in the same order as their declarations in the
  class/function

## [1.0.3] - 2023-03-30

### Fixed

- `kdoc-wrapping` will no longer wrap (break) Markdown tables in kdoc
- `kdoc-wrapping` will now wrap block quotes in kdoc

## [1.0.2] - 2023-03-30

### Fixed

- Indented code blocks which contain triple backticks inside the code block will no longer be
  indented by an extra space.

## [1.0.1] - 2023-03-29

### Added

- New editorconfig property `ktlint_kt-rules_wrapping_style` introduced to configure the KDoc text
  wrapping algorithm used by the `KDocWrappingRule`. The available options are `equal` (default) for
  the minimum raggedness algorithm and `greedy` for the greedy wrapping algorithm.
  - The greedy algorithm wraps the text by simply adding as many words as possible to a line
    without exceeding the maximum line length.
    - Example output (max line length: 30):
      ```
      This is a sample sentence that is
      wrapped using the greedy
      algorithm.
      ```
  - The minimum raggedness algorithm balances the line lengths more evenly, reducing the "
    raggedness" of the wrapped text.
    - Example output (max line length: 30):
      ```
      This is a sample sentence that
      is wrapped using the minimum
      raggedness algorithm.
      ```

### Changed

- updated behavior of `KDocWrappingRule`:
  - The rule will now be disabled if the `max_line_length` editorconfig property is not set. This
    was done to be more consistent with the behavior of first-party KtLint rules.
  - The rule now supports both minimum raggedness and greedy wrapping algorithms. The default
    algorithm has been changed from the greedy algorithm to the minimum raggedness algorithm for
    better line length balance in wrapped KDoc text. This option is set via
    the `ktlint_kt-rules_wrapping_style` editorconfig property.
- Updated the behavior of NoSinceInKDocRule:
  - The rule will now be silently disabled if the `project_version` property is not set in the
    editorconfig or as a `ktrules.project_version` `System` property.
  - The `project_version` editorconfig property has been renamed
    to `ktlint_kt-rules_project_version`.

## [1.0.0] - 2023-03-26

Hello World

[1.0.0]: https://github.com/rbusarow/ktrules/releases/tag/1.0.0
[1.0.1]: https://github.com/rbusarow/ktrules/releases/tag/1.0.1
[1.0.2]: https://github.com/rbusarow/ktrules/releases/tag/1.0.2
[1.0.3]: https://github.com/rbusarow/ktrules/releases/tag/1.0.3
[1.0.4]: https://github.com/rbusarow/ktrules/releases/tag/1.0.4
[1.0.5]: https://github.com/rbusarow/ktrules/releases/tag/1.0.5
[1.0.6]: https://github.com/rbusarow/ktrules/releases/tag/1.0.6
[1.0.7]: https://github.com/rbusarow/ktrules/releases/tag/1.0.7
[1.0.8]: https://github.com/rbusarow/ktrules/releases/tag/1.0.8
[1.0.9]: https://github.com/rbusarow/ktrules/releases/tag/1.0.9
[1.1.0]: https://github.com/rbusarow/ktrules/releases/tag/1.1.0
[1.1.1]: https://github.com/rbusarow/ktrules/releases/tag/1.1.1
[1.1.2]: https://github.com/rbusarow/ktrules/releases/tag/1.1.2
[1.1.3]: https://github.com/rbusarow/ktrules/releases/tag/1.1.3
