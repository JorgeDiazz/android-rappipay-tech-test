import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
  id("com.android.application")
  id("de.mannodermaus.android-junit5")
  kotlin("android")
  kotlin("kapt")
  id("dagger.hilt.android.plugin")
  id("androidx.navigation.safeargs.kotlin")
  kotlin("plugin.serialization")
}

android {
  compileSdk = Api.compileSDK
  defaultConfig {
    applicationId = "com.rappipay.app"
    minSdk = Api.minSDK
    targetSdk = Api.targetSDK
    versionCode = getNewVersionCode()
    versionName = getNewVersionName()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments += mapOf(
      "disableAnalytics" to "true",
      "clearPackageData" to "true"
    )
    multiDexEnabled = true
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
        "multidex-config.pro"
      )
    }
    getByName("debug") {
      isTestCoverageEnabled = true
      isPseudoLocalesEnabled = true
    }
  }

  flavorDimensions.addAll(listOf("version", "target"))
  productFlavors {
    create("staging") {
      dimension = "version"
      applicationIdSuffix = ".staging"
      versionNameSuffix = "-Staging"
      manifestPlaceholders["scheme"] = "rappipay.staging"
      buildConfigField("String", "SCHEME", "\"${manifestPlaceholders["scheme"]}\"")
      buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/\"")
      buildConfigField("String", "TMDB_API_KEY", "\"${gradleLocalProperties(rootDir).getProperty("TMDB_API_KEY")}\"")
    }
    create("production") {
      dimension = "version"
      manifestPlaceholders["scheme"] = "rappipay.production"
      buildConfigField("String", "SCHEME", "\"${manifestPlaceholders["scheme"]}\"")
      buildConfigField("String", "BASE_URL", "\"https://api.themoviedb.org/\"")
      buildConfigField("String", "TMDB_API_KEY", "\"${gradleLocalProperties(rootDir).getProperty("TMDB_API_KEY")}\"")
    }
    create("internal") {
      dimension = "target"
    }
    create("external") {
      dimension = "target"
    }

    androidComponents {
      beforeVariants { variantBuilder ->
        val version = variantBuilder.productFlavors.find { it.first == "version" }?.second
        val target = variantBuilder.productFlavors.find { it.first == "target" }?.second

        if (version == "staging" && target == "external") {
          variantBuilder.enable = false
        }
      }
    }
  }

  lint {
    abortOnError = false
  }

  buildFeatures {
    viewBinding = true
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  testOptions {
    unitTests.isIncludeAndroidResources = true
    unitTests.isReturnDefaultValues = true
    animationsDisabled = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
  }
}

dependencies {
  implementation(project(":base"))
  implementation(project(":components"))
  implementation(project(":core"))

  implementation(project(":home"))

  implementation(project(":movies"))
  implementation(project(":movies-domain"))
  implementation(project(":movies-entities"))

  implementation(Libraries.multidex)

  implementation(AnnotationProcessors.daggerHilt)
  kapt(AnnotationProcessors.daggerHiltAndroidCompiler)
  implementation(AnnotationProcessors.daggerHiltViewModel)

  kapt(Libraries.kotlinxMetadata)

  kapt(AnnotationProcessors.moshiCodegen)
  kapt(Libraries.lifeCycleCompiler)

  implementation(Libraries.kotlinJDK)
  implementation(Libraries.appcompat)
  implementation(Libraries.androidXCore)
  implementation(Libraries.constraintLayout)
  implementation(Libraries.material)
  implementation(Libraries.preferences)
  coreLibraryDesugaring(Libraries.desugarJdkLibs)

  implementation(Libraries.splashScreen)

  implementation(Libraries.moshi)

  implementation(Libraries.retrofit)
  implementation(Libraries.retrofitMoshi)
  implementation(platform(Libraries.okHttpBoM))
  implementation(Libraries.okHttpInterceptor)
  implementation(Libraries.okHttp)

  implementation(Libraries.coroutines)
  implementation(Libraries.coroutinesCore)

  implementation(Libraries.paging)

  implementation(Libraries.lifeCycleExtensions)
  implementation(Libraries.lifeCycleRuntime)
  implementation(Libraries.lifeCycleLiveData)
  implementation(Libraries.lifeCycleProcess)
  implementation(Libraries.lifeCycleViewModel)
  implementation(Libraries.lifeCycleViewModelKtx)

  implementation(Libraries.navigationFragment)
  implementation(Libraries.navigationUi)

  implementation(Libraries.roomRuntime)
  implementation(Libraries.roomKtx)
  kapt(Libraries.roomCompiler)

  implementation(Libraries.timber)

  implementation(Libraries.jsonSerialization)

  debugImplementation(Libraries.leakCanary)

  kaptTest(AnnotationProcessors.moshiCodegen)
  testImplementation(Libraries.mockWebServer)
  Libraries.suiteTest.forEach { testImplementation(it) }
  testRuntimeOnly(Libraries.jUnit5Engine)

  debugImplementation(Libraries.okReplay)
  releaseImplementation(Libraries.okReplayNoop)
  androidTestImplementation(Libraries.okReplayEspresso)

  androidTestImplementation(Libraries.jUnitExtKtx)
  androidTestImplementation(Libraries.testCoreKtx)
  androidTestImplementation(Libraries.androidXRunner)
  androidTestImplementation(Libraries.espressoCore)
  androidTestImplementation(Libraries.androidXRules)
  androidTestImplementation(Libraries.barista) {
    exclude(group = "org.jetbrains.kotlin")
  }
  androidTestUtil(Libraries.orchestrator)
}

fun getNewVersionCode(): Int {
  val versionCode = if (project.hasProperty("version_code")) {
    project.properties["version_code"].toString().toIntOrNull()
  } else {
    null
  }
  return versionCode ?: 1
}

fun getNewVersionName(): String {
  return if (project.hasProperty("version_name")) {
    project.properties["version_name"].toString()
  } else {
    "v1.0-Dirty"
  }
}
