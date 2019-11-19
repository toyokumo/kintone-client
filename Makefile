.PHONY: test

clean:
	lein clean

test-clj:
	lein test

test-cljs:
	lein test:cljs

test:
	lein do clean, test, test:cljs
