plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "it.kituwa.toolshed"
    compileSdk = 35

    defaultConfig {
        applicationId = "it.kituwa.toolshed"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    androidResources {
        // The catalogue is English only, and the default Play-style resources
        // from AndroidX are not wanted either.
        localeFilters += listOf("en")
    }

    buildTypes {
        release {
            // The catalogue is a few hundred kilobytes of text. Without R8 the
            // APK shipped ~20 MB of unstripped Compose.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    lint {
        abortOnError = true
        warningsAsErrors = false
        checkDependencies = false
        // Three checks that only ever say "a newer version exists". They are
        // not defects, and a report full of them trains you to ignore the
        // report. Versions here are pinned deliberately: F-Droid builds from
        // this file, and an unattended dependency bump is a build risk, not a
        // feature. targetSdk stays at 35 for the same reason, since that is the
        // level the F-Droid builder is guaranteed to have.
        disable += setOf(
            "GradleDependency",
            "AndroidGradlePluginVersion",
            "OldTargetApi",
        )
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    // Core icons only. material-icons-extended adds around two thousand icons
    // as generated Kotlin, which with minification off cost this app 42 MB of
    // dex. The six icons not in core live in res/drawable instead.
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
