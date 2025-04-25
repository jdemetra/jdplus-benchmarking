# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2025-04-25

This is a major release of benchmarking extensions for JDemetra+ v3.  
[JDemetra+ v3.5.0 or later](https://github.com/jdemetra/jdplus-main) version is required to run it.

### Added

- ![STAT] Add missing options in the growth rate preservation method
- ![STAT] Add disaggregation and interpolation of series with any periodicity (provided that the high frequency is a multiple of the low frequency)
- ![STAT] Add benchmarking (denton) of series with any periodicity (provided that the high frequency is a multiple of the low frequency)

### Changed

- ![STAT] Split disaggregation and interpolation
- ![OTHER] Modernize use of NIO API
- ![OTHER] Bump jdplus-main from 3.2.4 to [3.5.0](https://github.com/jdemetra/jdplus-main/releases/tag/v3.5.0)

## [1.2.1] - 2024-07-12

## [1.2.0] - 2024-07-12

## [1.1.0] - 2023-12-12

## [1.0.0] - 2023-12-12

[Unreleased]: https://github.com/jdemetra/jdplus-benchmarking/compare/v2.0.0...HEAD
[2.0.0]: https://github.com/jdemetra/jdplus-benchmarking/compare/v1.2.1...v2.0.0
[1.2.1]: https://github.com/jdemetra/jdplus-benchmarking/compare/v1.2.0...v1.2.1
[1.2.0]: https://github.com/jdemetra/jdplus-benchmarking/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/jdemetra/jdplus-benchmarking/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/jdemetra/jdplus-benchmarking/releases/tag/v1.0.0
[STAT]: https://img.shields.io/badge/-STAT-068C09
[OTHER]: https://img.shields.io/badge/-OTHER-e4e669
[IO]: https://img.shields.io/badge/-IO-F813F7
[UI]: https://img.shields.io/badge/-UI-5319E7
