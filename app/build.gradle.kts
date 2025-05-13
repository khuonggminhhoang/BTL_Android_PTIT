plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.foodorderapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.foodorderapp"
        minSdk = 24
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
    // Thêm khối này nếu bạn gặp lỗi liên quan đến duplicate classes khi dùng Java 11+
    // packagingOptions {
    //     resources {
    //         excludes += "/META-INF/{AL2.0,LGPL2.1}"
    //     }
    // }
}

dependencies {

    // Các thư viện AndroidX và Material cơ bản (giả định đã định nghĩa trong libs.versions.toml)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.recyclerview)

    // --- Glide Dependencies ---
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler) // Giữ nguyên nếu bạn dùng Java
    // Hoặc nếu bạn dùng Kotlin và KAPT:
    // kapt(libs.glide.compiler)


    // --- Volley (Nếu vẫn còn sử dụng) ---
    implementation("com.android.volley:volley:1.2.1")

    // --- Retrofit & Gson ---
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Kiểm tra phiên bản mới nhất nếu muốn
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Kiểm tra phiên bản mới nhất nếu muốn

    // --- OkHttp & Logging Interceptor (THÊM VÀO) ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // Hoặc phiên bản OkHttp3 mới nhất
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Phiên bản này nên khớp với phiên bản okhttp ở trên

    // Dependencies cho Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
