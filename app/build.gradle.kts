import java.text.SimpleDateFormat
import java.util.Date
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.example.baseproject"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.baseproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true

        val formattedDate = SimpleDateFormat("MM.dd.yyyy").format(Date())
        base.archivesName = "ProjectBase_v${versionName}(${versionCode})_${formattedDate}"
    }

    signingConfigs {
        create("release") {
            storeFile = file("key/projectbase.jks")
            storePassword = "123456"
            keyAlias = "key"
            keyPassword = "123456"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    lint {
        disable.addAll(
            hashSetOf(
                "ContentDescription",
                "HardcodedText",
                "SpUsage",
                "KotlinNullnessAnnotation",
                "NotifyDataSetChanged",
                "SetTextI18n",
                "DefaultLocale",
                "GradleDynamicVersion",
                "CustomSplashScreen",
                "ClickableViewAccessibility",
                "LockedOrientationActivity",
                "StaticFieldLeak"
            )
        )
    }

    bundle {
        language {
            enableSplit = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Compose BOM & UI libraries
    implementation(platform("androidx.compose:compose-bom:2025.12.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.12.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.60.1")
    kapt("com.google.dagger:hilt-compiler:2.60.1")

    // Room Database
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    kapt("androidx.room:room-compiler:2.8.5")

    // Coroutines & Lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.11.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.11.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0")

    // Networking & Parsing
    implementation("com.google.code.gson:gson:2.14.0")
    implementation("com.squareup.okhttp3:okhttp:5.3.2")
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.2")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // UI Utilities & Third-party
    implementation("com.github.bumptech.glide:glide:5.0.9")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.wdsqjq:AndRatingBar:1.0.6") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }
}
