plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}


android {
    namespace = "com.yeslab.sirus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yeslab.sirus"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.firebase:firebase-auth:22.2.0")
    implementation ("com.intuit.sdp:sdp-android:1.0.6")
    implementation ("com.makeramen:roundedimageview:2.3.0")
    implementation ("com.google.firebase:firebase-messaging:23.2.1")
    implementation ("com.google.firebase:firebase-database:20.2.2")
    implementation ("com.google.firebase:firebase-firestore:24.7.1")
    implementation ("com.google.firebase:firebase-storage:20.2.1")
    implementation ("com.google.firebase:firebase-analytics:21.3.0")
    implementation ("com.google.firebase:firebase-crashlytics:18.4.1")
    implementation("com.google.firebase:firebase-config-ktx:21.4.1")

    implementation ("androidx.multidex:multidex:2.0.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-scalars:2.9.0")

    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.squareup.picasso:picasso:2.71828")

    implementation ("io.github.muddz:styleabletoast:2.4.0")

    implementation ("dev.shreyaspatil.MaterialDialog:MaterialDialog:2.2.3")

    implementation ("org.aviran.cookiebar2:cookiebar2:1.1.5")
    implementation ("com.android.volley:volley:1.2.1")

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.firebase:firebase-iid:21.1.0")

    implementation ("com.github.addisonelliott:SegmentedButton:3.1.9")

    implementation ("xyz.schwaab:avvylib:1.2.0")
    implementation ("com.github.yukuku:ambilwarna:2.0.1")

    implementation ("com.github.mreram:showcaseview:1.4.0")
    implementation ("com.github.TutorialsAndroid:KAlertDialog:v14.0.19")

    implementation ("com.github.bumptech.glide:glide:4.13.0")

    implementation ("com.google.android.play:core:1.10.3")

    implementation ("com.github.Shashank02051997:FancyAlertDialog-Android:0.3")

    implementation ("io.github.chaosleung:pinview:1.4.4")
    implementation ("com.github.chivorns:smartmaterialspinner:1.5.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")

    implementation ("com.github.guilhe:circular-progress-view:1.2.1")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.6.0-alpha04")
    implementation ("androidx.navigation:navigation-ui-ktx:2.6.0-alpha04")

    implementation ("com.github.pchmn:MaterialChipsInput:1.0.8")
    implementation ("com.karumi:dexter:6.0.1")

    implementation ("io.insert-koin:koin-android:3.2.0-beta-1")
    implementation ("io.insert-koin:koin-androidx-navigation:3.2.0-beta-1")
    testImplementation("io.insert-koin:koin-test-junit4:3.2.0-beta-1")
    implementation ("android.arch.persistence.room:runtime:1.1.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("com.github.chivorns:smartmaterialspinner:1.5.0")

    implementation ("com.karumi:dexter:6.0.1")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.6.4")

    implementation ("com.airbnb.android:lottie:5.2.0")

    implementation ("com.github.yunusemresorkac:FastPrefs:1.0")
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")




    implementation ("com.android.billingclient:billing:6.0.1")
    implementation ("com.github.Spikeysanju:MotionToast:1.4")

    implementation ("com.params.stepview:stepview:1.0.2")

    implementation ("com.unity3d.ads:unity-ads:4.6.0")



}