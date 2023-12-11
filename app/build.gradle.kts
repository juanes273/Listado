plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.listado"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.listado"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packagingOptions {
        resources {
            excludes.add("/META-INF/AL2.0")
            excludes.add("/META-INF/LGPL2.1")
            excludes.add("/META-INF/NOTICE.md")
            excludes.add("/META-INF/LICENSE.md")
            excludes.add("/META-INF/DEPENDENCIES")
            excludes.add("/META-INF/INDEX.LIST")
            excludes.add("/META-INF/io.netty.versions.properties")


        }
    }
}

dependencies {

    //Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    //Hilt
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")

    // Iconos
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.compose.material3:material3:1.0.0-beta01")

    //javamail
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // Dependencias de Gmail API
    implementation ("com.google.api-client:google-api-client-android:1.32.1")
    implementation ("com.google.oauth-client:google-oauth-client-jetty:1.32.1")
    implementation ("com.google.apis:google-api-services-gmail:v1-rev110-1.25.0")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.20.0")
    implementation ("com.google.http-client:google-http-client-android:1.39.2")

    implementation ("software.amazon.awssdk:sns:2.16.0")

// Commons Codec para Base64 (Apache Commons)
    implementation ("commons-codec:commons-codec:1.15")
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")

    implementation ("androidx.appcompat:appcompat:1.3.0")

    //Firebase Storage
    implementation ("com.google.firebase:firebase-storage:11.5.3")

    implementation("io.coil-kt:coil-compose:1.3.2")


}
