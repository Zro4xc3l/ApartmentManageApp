plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.apartmentmanageapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.apartmentmanageapp"
        minSdk = 23
        targetSdk = 35
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
    // Core AndroidX libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment:2.6.0")
    implementation("androidx.navigation:navigation-ui:2.6.0")

    // UI Components
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Firebase BOM (Manages Firebase versions)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Firebase Services (NO Google Play Services dependency)
    implementation("com.google.firebase:firebase-auth")       // Authentication (Email/Password)
    implementation("com.google.firebase:firebase-firestore")  // Firestore Database
    implementation("com.google.firebase:firebase-storage")    // Cloud Storage

    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Material Components
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.google.android.gms:play-services-base:18.5.0")

}
