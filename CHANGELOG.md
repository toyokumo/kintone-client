# Change Log

## Unreleased

## 0.6.3
### Added
- Add support for User API token authentication with Bearer authorization header. ([#23](https://github.com/toyokumo/kintone-client/pull/23))

## 0.6.2
### Changed
- Make `re-base-url` to public. ([#21](https://github.com/toyokumo/kintone-client/pull/21))

## 0.6.1
### Changed
- Bump outdated dependencies. ([#20](https://github.com/toyokumo/kintone-client/pull/20))

## 0.6.0
### Added
- Add support App API ([#15](https://github.com/toyokumo/kintone-client/pull/15))
- Add support User API ([#16](https://github.com/toyokumo/kintone-client/pull/16))

## 0.5.1
### Changed
- Bump clj-http to `3.10.1`.

### Fixed
- Fixed `kintone-client.record/file-upload` to work correctly with multibyte filename.

## 0.5.0
### Fixed
- Add CSRF Token and `X-Requested-With` header to each request of connection of ClojureScript ([#12](https://github.com/toyokumo/kintone-client/pull/12))
### Breaking
- Rename this project `kintone-client` instead of `kintone-clj`

## 0.4.0
### Added
- See Content-Type of the error response ([#9](https://github.com/toyokumo/kintone-client/pull/9))
- Add `handler` and `error-handler` options to `new-connection` ([#10](https://github.com/toyokumo/kintone-client/pull/10))

## 0.3.0
### Added
- Add support App API ([#8](https://github.com/toyokumo/kintone-client/pull/8))

## 0.2.0
### Added
- Add some tests only work on dev profile ([#4](https://github.com/toyokumo/kintone-client/pull/4))
- Add URL utilities that is copied from [cybozu-http-clj](https://github.com/ayato-p/cybozu-http-clj/blob/master/src/cybozu_http/kintone/url.clj) ([#5](https://github.com/toyokumo/kintone-client/pull/5))

### Breaking
- Make it always `true` the value of `totalCount` which is used on `get-records-by-query` API ([#6](https://github.com/toyokumo/kintone-client/pull/6))

### Fixed
- Improve some documents ([#7](https://github.com/toyokumo/kintone-client/pull/7))

## 0.1.2
### Added
- Add as argument to `file-downlaod` API ([#1](https://github.com/toyokumo/kintone-client/pull/1))

### Fixed
- `totalCount` case on `get-records-by-query` ([#2](https://github.com/toyokumo/kintone-client/pull/2))

### Breaking
- (Only Clojure) Change `file-download` default response body type from String to byte array ([#1](https://github.com/toyokumo/kintone-client/pull/1))

## 0.1.1 - 2019-09-04
### Fixed
- Make dynamic function name surrounded by `*`

## 0.1.0 - 2019-09-04

Initial public version.

### Added
- SDK of kintone Record API
