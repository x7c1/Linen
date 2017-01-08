
resolvers += Resolver.url(
  "bintray-x7c1-android",
  url("http://dl.bintray.com/x7c1/android"))(Resolver.ivyStylePatterns)

addSbtPlugin("x7c1" % "wheat-harvest" % "0.1.0")
