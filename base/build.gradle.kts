plugins {
  id("java-library")
  id("kotlin")
  kotlin("kapt")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(Libraries.kotlinJDK)

  implementation(Libraries.coroutines)
  implementation(Libraries.coroutinesCore)

  implementation(Libraries.moshi)
  kapt(AnnotationProcessors.moshiCodegen)
}
