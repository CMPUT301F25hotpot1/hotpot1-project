plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lottary"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lottary"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ✅ 推荐：让 Espresso 测试更稳定
    testOptions {
        animationsDisabled = true
    }
}

dependencies {

    // ✅ ✅ Firebase BOM (ONLY ONE)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // ✅ Firebase modules
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // ✅ AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.activity:activity:1.9.3")

    // ✅ CameraX
    val camerax = "1.3.4"
    implementation("androidx.camera:camera-core:$camerax")
    implementation("androidx.camera:camera-camera2:$camerax")
    implementation("androidx.camera:camera-lifecycle:$camerax")
    implementation("androidx.camera:camera-view:$camerax")

    // ✅ ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // ✅ Images
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.squareup.picasso:picasso:2.71828")

    // ✅ Unit Test
    testImplementation("junit:junit:4.13.2")

    // ✅ ✅ ✅ Android Instrumented Test Dependencies
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ✅ Required for ActivityTestRule
    androidTestImplementation("androidx.test:rules:1.5.0")

    // ✅ Required for Espresso test runner
    androidTestImplementation("androidx.test:runner:1.5.2")

    // ✅ For testing intents between activities
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

    // ✅ For RecyclerViewActions (swipe, scrollToPosition, click button inside item)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
}