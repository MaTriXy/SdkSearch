SDK Search (Webapp)
===================

Building
--------

### Local

Running `./gradlew -t :frontend:web:run` will start a server at http://localhost:8088 with
automatic recompile and reload.

Unfortunately, due to CORS restrictions on the d.android.com resource which powers this application,
this site only works in Safari when "Disable cross-origin restrictions" is checked.


### Release

There is no formal release build set up right now and most of plugins lack the necessary nobs to
vary settings correctly to create one. The easiest way to get a release JS is to change the `mode`
defined in `build.gradle` to 'production' and run `:frontend:web:build` and then deploy
`build/distributions/sdk-search.zip` to the file host.

`src/main/proxy.js` also needs to be deployed manually to a Cloudflare worker (if changed).
