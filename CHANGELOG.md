# Changelog

## 1.0.1-SNAPSHOT (unreleased)

### Added

- New editorconfig property `ktlint_kt-rules_wrapping_style` introduced to configure the KDoc text
  wrapping algorithm used by the `KDocWrappingRule`. The available options are `equal` (default) for
  the minimum raggedness algorithm and `greedy` for the greedy wrapping algorithm.
  - The greedy algorithm wraps the text by simply adding as many words as possible to a line without
    exceeding the maximum line length.
    - Example output (max line length: 30):
      ```
      This is a sample sentence that is
      wrapped using the greedy
      algorithm.
      ```
  - The minimum raggedness algorithm balances the line lengths more evenly, reducing the "raggedness"
    of the wrapped text.
    - Example output (max line length: 30):
      ```
      This is a sample sentence that
      is wrapped using the minimum
      raggedness algorithm.
      ```

### Changed

- updated behavior of `KDocWrappingRule`:
  - The rule will now be disabled if the `max_line_length` editorconfig property is not set. This was
    done to be more consistent with the behavior of first-party KtLint rules.
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
