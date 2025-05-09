import java.net.URL
import java.util.Base64

plugins {
  `maven-publish`
  signing
  alias(libs.plugins.kotlin)
  alias(libs.plugins.dokka)
  alias(libs.plugins.nexus)
}

data class SemVer(
  val major: Int,
  val minor: Int,
  val patch: Int,
) {
  inline val featureVersion get() = "$major.$minor.0"

  inline val gitTag get() = "v$major.$minor.$patch"

  inline val sedExp get() = "SemVer(major = $major, minor = $minor, patch = $patch)"

  override fun toString() = "$major.$minor.$patch"
}

val projectVersion = SemVer(major = 1, minor = 1, patch = 1)

group = "io.foxcapades.kt"
version = projectVersion.toString()

repositories {
  mavenCentral()
}

dependencies {
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(11)
}

repositories {
  mavenCentral()
}

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.AMAZON
  }

  java {
    withSourcesJar()
    withJavadocJar()
  }
}

dependencies {
//  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events(
//      TestLogEvent.PASSED,
//      TestLogEvent.FAILED,
//      TestLogEvent.SKIPPED,
    )

    showStandardStreams = true
  }
}

tasks.dokkaHtml {
  outputDirectory = file("docs/dokka")
  suppressObviousFunctions.set(true)
  dokkaSourceSets.configureEach {
    sourceLink {
      localDirectory.set(projectDir.resolve("src"))
      remoteUrl.set(URL("https://github.com/Foxcapades/kdbc/tree/main/src"))
      remoteLineSuffix.set("#L")
    }
  }
}

mavenCentral {
  authToken = Base64.getUrlEncoder().encodeToString("${project.properties["nexus.user"]}:${project.properties["nexus.pass"]}".toByteArray())
}

publishing {
  publications {
    create<MavenPublication>("gpr") {
      from(components["java"])
      pom {
        name.set("Kotlin JDBC Extensions")
        description.set("Provides extension methods for the JDBC API to" +
        " enable more Kotlin-esque construction and usage of various JDBC" +
        " types and methods.")
        url.set("https://github.com/foxcapades/kdbc")

        licenses {
          license {
            name.set("MIT")
          }
        }

        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("foxcapades.io@gmail.com")
            url.set("https://github.com/foxcapades")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/foxcapades/kdbc.git")
          developerConnection.set("scm:git:ssh://github.com/foxcapades/kdbc.git")
          url.set("https://github.com/foxcapades/kdbc")
        }
      }
    }
  }
}

signing {
  useGpgCmd()

  sign(configurations.archives.get())
  sign(publishing.publications["gpr"])
}

tasks.register("bump-feature") {
  group = "Custom"

  doLast {
    val newVersion = projectVersion.copy(minor = projectVersion.minor + 1, patch = 0)

    with(ProcessBuilder("sed", "s/${projectVersion.sedExp}/${newVersion.sedExp}/", "build.gradle.kts")) {
      val tmp = file("build.gradle.kts.tmp")
      redirectOutput(tmp)

      if (start().waitFor() != 0) {
        throw GradleException("failed to patch gradle file!")
      }

      val cur = file("build.gradle.kts")
      cur.delete()
      tmp.renameTo(cur)
    }
  }
}

tasks.register("release") {
  group = "Custom"

  doFirst {
    with(ProcessBuilder("git", "diff-index", "--quiet", "HEAD")) {
      if (start().waitFor() != 0) {
        throw GradleException("git workspace is not clean!")
      }
    }
  }

  finalizedBy("publishToMavenCentralPortal")
}

tasks.register("update-readme") {
  group = "Custom"

  doLast {
    val tmp    = file("readme.adoc.tmp")
    val readme = file("readme.adoc")

    tmp.delete()
    tmp.bufferedWriter().use { w ->
      readme.bufferedReader().use { r ->
        r.lineSequence()
          .map {
            when {
              !it.startsWith(':')                -> it
              it.startsWith(":version-actual:")  -> ":version-actual: $version"
              it.startsWith(":version-feature:") -> ":version-feature: ${projectVersion.featureVersion}"
              else                               -> it
            }
          }
          .forEach {
            w.write(it)
            w.newLine()
          }
      }
    }

    readme.delete()
    tmp.renameTo(readme)
  }
}

tasks.withType<tech.yanand.gradle.mavenpublish.PublishToCentralPortalTask> {
  dependsOn("release")
  finalizedBy("update-readme")
}
