test-clj:
	@lein test

test-cljs:
	@lein test:cljs

test: test-clj test-cljs
