plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.abc.campushub"
    compileSdk = 35
    packagingOptions {
        exclude ("META-INF/DEPENDENCIES")
    }
    defaultConfig {
        applicationId = "com.abc.campushub"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.swiperefreshlayout)
    implementation(libs.play.services.cast.framework)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.google.android.gms:play-services-auth:20.6.0")
    implementation("com.google.api-client:google-api-client-android:1.31.2")
    implementation("com.google.apis:google-api-services-drive:v3-rev20211107-1.32.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-firestore:24.9.0")
    implementation("com.google.firebase:firebase-auth:22.1.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-core:21.1.1")
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
}