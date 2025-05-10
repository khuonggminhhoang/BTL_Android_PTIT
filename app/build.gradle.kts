plugins {
    alias(libs.plugins.android.application)
    // Nếu bạn dùng Kotlin, bạn sẽ cần thêm plugin kapt ở đây:
    // alias(libs.plugins.kotlin.kapt) // Giả sử bạn định nghĩa nó trong libs.versions.toml
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
    //         excludes += '/META-INF/{AL2.0,LGPL2.1}'
    //     }
    // }
}

dependencies {

    // Các thư viện AndroidX và Material cơ bản (giả định đã định nghĩa trong libs.versions.toml)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)      // Hoặc libs.androidx.activity nếu bạn đặt tên khác
    implementation(libs.constraintlayout) // Hoặc libs.androidx.constraintlayout

    // Thư viện RecyclerView (cần thiết cho danh sách)
    implementation(libs.recyclerview) // <<< THÊM DÒNG NÀY (Giả sử bạn đã định nghĩa trong libs.versions.toml)

    // --- Glide Dependencies ---
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Dependencies cho Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}