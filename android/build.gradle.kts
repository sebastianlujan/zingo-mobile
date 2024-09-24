// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    ext {
        buildToolsVersion = "34.0.0"
        minSdkVersion = 24
        compileSdkVersion = 34
        targetSdkVersion = 34
        ndkVersion = "23.2.8568313"
        kotlinVersion = '1.9.0'
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath('com.android.tools.build:gradle:8.1.1')
        classpath("com.facebook.react:react-native-gradle-plugin")
	    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion"
    }
}

allprojects {
   repositories {
     google()
     maven {
       url("$rootDir/../node_modules/detox/Detox-android")
     }
     maven { url 'https://www.jitpack.io' }
   }
 }

// this is not the best solution, but while the libraries have no namespace...
// https://discuss.gradle.org/t/namespace-not-specified-for-agp-8-0-0/45850/5

//subprojects {
//   afterEvaluate { project ->
//       if (project.hasProperty('android')) {
//           project.android {
//               if (namespace == null) {
//                   namespace project.group
//               }
//           }
//       }
//   }
//}