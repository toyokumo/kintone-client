.PHONY: test

lint:
	clj-kondo --lint src:test

format-check:
	cljstyle check --report -v

clean:
	lein clean

test-clj:
	lein test

test-cljs:
	lein test:cljs

test:
	lein do clean, test, test:cljs
