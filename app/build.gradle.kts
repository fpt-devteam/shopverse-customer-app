import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.google.gms.google-services")
    alias(libs.plugins.android.application)
}

// Read API Keys from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val mapsApiKey: String = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: "YOUR_API_KEY_HERE"
val supabaseUrl: String = localProperties.getProperty("SUPABASE_URL") ?: "https://shopverse.supabase.co"
val supabaseAnonKey: String = localProperties.getProperty("SUPABASE_ANON_KEY") ?: "YOUR_SUPABASE_ANON_KEY"

android {
    namespace = "com.example.shopverse_customer_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shopverse_customer_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject API keys as manifest placeholders
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey

        // Make API keys available as BuildConfig fields
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.libraries.places:places:3.3.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // OkHttp for API calls (Directions API)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Retrofit for Supabase REST API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // EncryptedSharedPreferences for secure token storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.messaging)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // FlexboxLayout for flexible chip layouts
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
}