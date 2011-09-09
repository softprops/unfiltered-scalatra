organization := "com.example"

name := "My Web Project"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
   "net.databinder" %% "unfiltered-filter" % "0.4.1",
   "net.databinder" %% "unfiltered-jetty" % "0.4.1",
   "org.clapper" %% "avsl" % "0.3.1"
)

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2"
)
