plugins {
    // Estas linhas de plugin devem estar corretas
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ifpr.androidapptemplate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ifpr.androidapptemplate"
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
        // Garantindo compatibilidade com Kotlin 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        // Ativando o View Binding para acessar os elementos de layout (como no HomeFragment)
        viewBinding = true
    }
}

dependencies {

    // --- ANDROIDX E NAVEGAÇÃO (Mantidas com libs. para simplificar) ---
    // Se estas linhas falharem, mude-as para a sintaxe de string: implementation("androidx.core:core-ktx:1.13.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- FIREBASE (Usando o BOM para gerenciar versões) ---
    // O BOM (Bill of Materials) define as versões de todo o Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Dependências do Firebase que você está usando
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-common-ktx")

    // --- GOOGLE SERVICES (MAPAS E LOCALIZAÇÃO) ---
    // 🛑 Adiciona o serviço de mapas para exibir o MapItemFragment (tela branca)
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    // Localização GPS (usado para getCurrentLocation no HomeFragment)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // Autenticação (Login)
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // --- OUTRAS ---
    implementation(libs.glide)
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    // implementation(libs.firebase.crashlytics.buildtools) // Removido ou comentado se não for essencial agora

    // --- TESTES ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}