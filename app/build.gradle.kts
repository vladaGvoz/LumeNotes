plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lumenotes"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.lumenotes"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation(libs.activity)
    annotationProcessor ("androidx.room:room-compiler:2.6.1") // for Java
    // Optional: if using Kotlin, use kapt instead of annotationProcessor

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // AdMob (Google Mobile Ads)
    implementation("com.google.android.gms:play-services-ads:22.2.0")

    implementation("com.google.android.material:material:1.11.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}
