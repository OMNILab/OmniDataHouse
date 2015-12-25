name := "WifiToolkit"

version := "1.0"

scalaVersion := "2.10.4" // For Spark 1.3.0

conflictManager := ConflictManager.latestRevision

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.2.0" % "provided"

libraryDependencies += "joda-time" % "joda-time" % "2.6"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

libraryDependencies += "org.yaml" % "snakeyaml" % "1.15"
