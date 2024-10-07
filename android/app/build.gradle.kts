import groovy.lang.Closure

plugins {
    id("com.android.application")
    id("com.facebook.react")
    id("kotlin-android")
}

/**
 * This is the configuration block to customize your React Native Android app.
 * By default you don't need to apply any configuration, just uncomment the lines you need.
 */
react {
    /* Folders */
    //   The root of your project, i.e. where "package.json" lives. Default is '..'
    // root = file("../")
    //   The folder where the react-native NPM package is. Default is ../node_modules/react-native
    // reactNativeDir = file("../node_modules/react-native")
    //   The folder where the react-native Codegen package is. Default is ../node_modules/@react-native/codegen
    // codegenDir = file("../node_modules/@react-native/codegen")
    //   The cli.js file which is the React Native CLI entrypoint. Default is ../node_modules/react-native/cli.js
    // cliFile = file("../node_modules/react-native/cli.js")
 
    /* Variants */    //   The list of variants to that are debuggable. For those we're going to
    //   skip the bundling of the JS bundle and the assets. By default is just 'debug'.
    //   If you add flavors like lite, prod, etc. you'll have to list your debuggableVariants.
    // debuggableVariants = ["liteDebug", "prodDebug"]
 
    /* Bundling */
    //   A list containing the node command and its flags. Default is just 'node'.
    // nodeExecutableAndArgs = ["node"]
    //
    //   The command to run when bundling. By default is 'bundle'
    // bundleCommand = "ram-bundle"
    //
    //   The path to the CLI configuration file. Default is empty.
    // bundleConfig = file(../rn-cli.config.js)
    //
    //   The name of the generated asset file containing your JS bundle
    // bundleAssetName = "MyApplication.android.bundle"
    //
    //   The entry file for bundle generation. Default is 'index.android.js' or 'index.js'
    // entryFile = file("../js/MyApplication.android.js")
    //
    //   A list of extra flags to pass to the 'bundle' commands.
    //   See https://github.com/react-native-community/cli/blob/main/docs/commands.md#bundle
    // extraPackagerArgs = []
 
    /* Hermes Commands */
    //   The hermes compiler command to run. By default it is 'hermesc'
    // hermesCommand = "$rootDir/my-custom-hermesc/bin/hermesc"
    //
    //   The list of flags to pass to the Hermes compiler. By default is "-O", "-output-source-map"
    // hermesFlags = ["-O", "-output-source-map"]
}
 
/**
 * Set this to true to Run Proguard on Release builds to minify the Java bytecode.
 */
var enableProguardInReleaseBuilds = false

/**
 * The preferred build flavor of JavaScriptCore (JSC).
 *
 * For example, to use the international variant, you can use:
 * `def jscFlavor = 'org.webkit:android-jsc-intl:+'`
 *
 * The international variant includes ICU i18n library and necessary data
 * allowing to use e.g. `Date.toLocaleString` and `String.localeCompare` that
 * give correct results when using with locales other than en-US.  Note that
 * this variant is about 6MiB larger per architecture than default.
 */
var jscFlavor = "org.webkit:android-jsc:+"

/**
 * Whether to enable building a separate APK for each ABI.
 *
 * Defaults to false but can be set to true with the project properties flag
 * e.g. ./gradlew assembleDebug -PsplitApk=true
 *
 * Additional option to include a universal APK
 * e.g. ./gradlew assembleRelease -PsplitApk=true -PincludeUniversalApk=true
 */
//val splitApk = rootProject.findProperty("splitApk")?.toString().toBoolean()?: false
val splitApk by lazy {
    (project.findProperty("splitApk") as? String)?.toBoolean() ?: false
}

val includeUniversalApk by lazy {
    (project.findProperty("includeUniversalApk") as?String)?.toBoolean() ?: false
}

android {
    var _ndkVersion = rootProject.extra["ndkVersion"]
    var _compileSdkVersion = rootProject.extra["compileSdkVersion"]
    var _minSdkVersion = rootProject.extra["minSdkVersion"]
    var _targetSdkVersion = rootProject.extra["targetSdkVersion"]

    namespace = "org.ZingoLabs.Zingo" //fun ?

    compileOptions {
        sourceCompatibility = JavaVersion.toVersion("18.0.2.1")
        targetCompatibility = JavaVersion.toVersion("18.0.2.1")
    }

    kotlinOptions {
        jvmTarget = "18"
    }

    defaultConfig {
        applicationId = namespace // Real
        minSdkVersion(_minSdkVersion as Int)
        targetSdkVersion(_targetSdkVersion as Int)

        versionCode = 187 // Real
        versionName = "zingo-1.4.3" // Real
        missingDimensionStrategy("react-native-camera", "general")
        testBuildType = System.getProperty("testBuildType", "debug")        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    splits {
        abi {
            isEnable = splitApk
            reset()
            include("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
            isUniversalApk = includeUniversalApk
        }
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "androisigningConfigsd"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
        create("release") {
            storeFile = file("release.keystore")
            storePassword = "androisigningConfigsd"
            keyAlias = "androiddebugkey"
            keyPassword = "androiddebugkey"
        }
    }

    packagingOptions {
        jniLibs {
            pickFirsts.addAll(
                listOf(
                    "lib/armeabi-v7a/libc++_shared.so", 
                    "lib/arm64-v8a/libc++_shared.so",
                    "lib/x86/libc++_shared.so",
                    "lib/x86_64/libc++_shared.so"
                )
            )
        }
    }

    buildTypes {       
        getByName("release") {
            // Caution! In production, you need to generate your own keystore file.
            // see https://reactnative.dev/docs/signed-apk-android.
            signingConfig = signingConfigs.getByName("release")

            isMinifyEnabled = enableProguardInReleaseBuilds
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
        /* 
        getByName("debug") {
            signingConfig = signingConfig.getByName("debug")
        }
        */
    }

    buildFeatures {
        viewBinding = true
    }

    testOptions {
        // Managed devices for gradle integration testing
        managedDevices {
            devices {
                create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api29_x86") {
                    device = "Pixel 2"
                    apiLevel = 29
                    systemImageSource = "aosp"
                    require64Bit = false
                }
                create<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30_x86_64") { //(com.android.build.api.dsl.ManagedVirtualDevice) {
                    device = "Pixel 2"
                    apiLevel = 30
                    systemImageSource = "aosp"
                    require64Bit = true
                }
            }
            groups {
                create("x86_Archs") {
                    targetDevices.add(devices.getByName("pixel2api29_x86"))
                    targetDevices.add(devices.getByName("pixel2api30_x86_64"))
                }
            }
        }
    }
}


// Map for the version code that gives each ABI a value.

extra.set(
    "abiCodes", 
    mapOf(
        "armeabi-v7a" to 1,
        "x86" to 2,
        "arm-v8a" to 3,
        "x86_64" to 4
    )
)
//ext.abiCodes = [armeabi-v7a':1, 'x86':2, 'arm64-v8a': 3, 'x86_64':4]

val FLIPPER_VERSION: String by project
val hermesEnabled: String by project
val kotlinVersion: String by project

// For each APK output variant, override versionCode with a combination of
// ext.abiCodes * 10000 + variant.versionCode. 
// variant.versionCode is equal to defaultConfig.versionCode.
// If you configure product flavors that define their own versionCode, 
// variant.versionCode uses that value instead.
android {
    applicationVariants.all {
        val variant = this
        outputs.map { output ->
            if (output is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val abiCodes: Map<String, Int> by project.extra
                val baseAbiVersionCode = abiCodes[output.filters.find { it.filterType == "ABI" }?.identifier]

                // Because abiCodes.get() returns null for ABIs that are not mapped by ext.abiCodes,
                // the following code doesn't override the version code for universal APKs.
                // However, because you want universal APKs to have the lowest version code,
                // this outcome is desirable.
                baseAbiVersionCode?.let { baseCode ->
                    // Assigns the new version code to versionCodeOverride, which changes the
                    // version code for only the output APK, not for the variant itself. Skipping
                    // this step causes Gradle to use the value of variant.versionCode for the APK.
                    output.versionCodeOverride = baseCode * 10000 + (variant.versionCode ?: 0)
                }
            }
        }
    }
}

android {
    applicationVariants.all {
        val variant = this
        // val taskName = "generate${variant.name.capitalize()}UniFFIBindings"
        
        // val task = tasks.register<Exec>(taskName) {
        //     workingDir("${rootProject.projectDir}/../rust")
        //     commandLine(
        //         "cargo", "run", "--release", "--features=uniffi/cli", "--bin", "uniffi-bindgen",
        //         "generate", "../rust/lib/src/zingo.udl", "--language", "kotlin",
        //         "--out-dir", "${buildDir}/generated/source/uniffi/${variant.name}/java"
        //     )
        // }
        // variant.javaCompileProvider.get().dependsOn(task)

        val sourceSet = variant.sourceSets.find { it.name == name }
        val uniffiGeneratedPath = "generated/source/uniffi/${variant.name}/java"
        //sourceSets["main"].java.srcDir(File(buildDir, uniffiGeneratedPath))

        // sourceSets.getByName(variant.name) {
        //     java.srcDir(File(buildDir, uniffiGeneratedPath))
        // }

        sourceSets {
            getByName("main") {
                java.srcDir(File(buildDir, uniffiGeneratedPath))
            }
        }
    }       
}
            
dependencies {
    // The version of react-native is set by the React Native Gradle Plugin
    implementation("com.facebook.react:react-android")

    androidTestImplementation("com.wix:detox:+")
    implementation("androidx.appcompat:appcompat:1.1.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")
    implementation("com.facebook.soloader:soloader:0.10.5+")

    
    // Detox tests getAttributes() needs this
    debugImplementation("com.google.android.material:material:1.2.1")

    debugImplementation("com.facebook.flipper:flipper:${FLIPPER_VERSION}")
    debugImplementation("com.facebook.flipper:flipper-network-plugin:${FLIPPER_VERSION}") {
        exclude(
            group="com.squareup.okhttp3",
            module="okhttp"
        )
    }

    debugImplementation("com.facebook.flipper:flipper-fresco-plugin:${FLIPPER_VERSION}")

    if (hermesEnabled.toBoolean()) {
        implementation("com.facebook.react:hermes-android")
    } else {
        implementation(jscFlavor)
    }

	implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    val work_version = "2.7.1"

    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")

    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // optional - RxJava2 support
    implementation("androidx.work:work-rxjava2:$work_version")

    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:$work_version")

    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:$work_version")

    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:$work_version")
    
    // google truth testing framework
    androidTestImplementation("com.google.truth:truth:1.1.3")
    
    // JSON parsing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.+")
    
    // JUnit test runners
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // Kotlin extensions for androidx.test.ext.junit
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.5")

    // uniffi needs this
    implementation("net.java.dev.jna:jna:5.9.0@aar")
}

apply(from = file("../../node_modules/@react-native-community/cli-platform-android/native_modules.gradle"))

val applyNativeModulesAppBuildGradle = extra.get("applyNativeModulesAppBuildGradle") as Closure<*>
applyNativeModulesAppBuildGradle(project)