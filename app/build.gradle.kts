plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.spenvolt.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.spenvolt.mobile"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes{
        debug {
            buildConfigField("String", "API_URL", "\"http://192.168.1.104:8080\"")
        }
        release {
            buildConfigField("String", "API_URL", "\"https://spenvolt-api-production.up.railway.app\"")

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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.recyclerview)
    implementation(libs.drawerlayout)
    implementation(libs.volley)
}
