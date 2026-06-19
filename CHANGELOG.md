# CHANGELOG

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] - 2026-06-19
### Added
- Compile-time transformation of methods returning `CompletableFuture` using annotation processor.
- Default value handling via `ParamType.LITERAL` and `ParamType.VARIABLE` specifications.
- Interface-based proxy mechanisms for `CompletableFuture` transformations.
- Profile-specific configurations for `CutCode` annotations enabling custom call replacements and parameter mappings.
- Example test cases in `test-app` demonstrating completed pipelines, default value overrides, and interface proxy usage.

## [2.0.0] - 2026-06-17
### Added
 - First release
