import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  java
  idea
  eclipse
  kotlin("jvm") version Globals.kotlinVersion
  id("com.github.ben-manes.versions") version Globals.Gradle.Plugin.versionsVersion
}

allprojects {
  version = Globals.Project.version
  group = Globals.Project.groupId

  apply(plugin = "base")

  repositories {
    mavenCentral()
  }

  tasks {
    getByName("clean") {
      doLast {
        delete(
            project.buildDir,
            "${project.projectDir}/out"/*,
            "${project.projectDir}/buildSrc/build"*/
        )
      }
    }
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "kotlin")

  java {
    sourceCompatibility = Globals.javaVersion
    targetCompatibility = Globals.javaVersion
  }

  configure<JavaPluginConvention> {
    sourceCompatibility = Globals.javaVersion
    targetCompatibility = Globals.javaVersion
  }

  repositories {
    mavenCentral()
  }

  dependencies {
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("junit:junit:${Globals.junitVersion}")
    testImplementation(platform("org.junit:junit-bom:${Globals.junitJupiterVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
    testRuntime("org.junit.platform:junit-platform-launcher")
  }

  tasks {
    withType<KotlinCompile> {
      kotlinOptions.jvmTarget = "${Globals.javaVersion}"
    }

    withType<Test> {
      useJUnitPlatform()
      testLogging {
        showExceptions = true
        showStandardStreams = true
        events(PASSED, SKIPPED, FAILED)
      }
    }
  }
}

tasks {
  withType<Wrapper> {
    gradleVersion = Globals.Gradle.wrapperVersion
    distributionType = Wrapper.DistributionType.BIN
  }

  // gradle dependencyUpdates -Drevision=release --parallel
  named<DependencyUpdatesTask>("dependencyUpdates") {
    resolutionStrategy {
      componentSelection {
        all {
          val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea", "SNAPSHOT")
              .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-+]*") }
              .any { it.matches(candidate.version) }
          if (rejected) reject("Release candidate")
        }
      }
    }
  }
}

defaultTasks(/*"clean", */"build")
