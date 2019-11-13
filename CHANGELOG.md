# Change Log

## Unreleased
- See Content-Type of the error response ([#9](https://github.com/toyokumo/kintone-clj/pull/9))
- Add `handler` and `error-handler` options to `new-connection` ([#10](https://github.com/toyokumo/kintone-clj/pull/10))

## 0.3.0
### Added
- Add support App API ([#8](https://github.com/toyokumo/kintone-clj/pull/8))

## 0.2.0
### Added
- Add some tests only work on dev profile ([#4](https://github.com/toyokumo/kintone-clj/pull/4))
- Add URL utilities that is copied from [cybozu-http-clj](https://github.com/ayato-p/cybozu-http-clj/blob/master/src/cybozu_http/kintone/url.clj) ([#5](https://github.com/toyokumo/kintone-clj/pull/5))

### Breaking
- Make it always `true` the value of `totalCount` which is used on `get-records-by-query` API ([#6](https://github.com/toyokumo/kintone-clj/pull/6))

### Fixed
- Improve some documents ([#7](https://github.com/toyokumo/kintone-clj/pull/7))

## 0.1.2
### Added
- Add as argument to `file-downlaod` API ([#1](https://github.com/toyokumo/kintone-clj/pull/1))

### Fixed
- `totalCount` case on `get-records-by-query` ([#2](https://github.com/toyokumo/kintone-clj/pull/2))

### Breaking
- (Only Clojure) Change `file-download` default response body type from String to byte array ([#1](https://github.com/toyokumo/kintone-clj/pull/1))

## 0.1.1 - 2019-09-04
### Fixed
- Make dynamic function name surrounded by `*`

## 0.1.0 - 2019-09-04

Initial public version.

### Added
- SDK of kintone Record API
