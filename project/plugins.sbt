// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository
resolvers += "Local Play Repository" at "file://"+Path.userHome.absolutePath+"/Downloads/play-2.1.3/repository/local/"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Typesafe cache repository
resolvers += "Typesafe repository cache" at "http://repo.typesafe.com/typesafe/repo1-cache/"

// Scale tools releases cached
resolvers += "Typesafe scala tools releases cache" at "http://repo.typesafe.com/typesafe/scala-tools-releases-cache/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("play" % "sbt-plugin" % "2.1.3")
