import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.screenshot)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    jacoco
}

val apiBaseUrl = providers.gradleProperty("API_BASE_URL")
    .getOrElse("https://api.example.invalid/")
require(apiBaseUrl.endsWith("/")) { "API_BASE_URL must end with '/': $apiBaseUrl" }

// 玩Android 只是当前临时认证源，后续替换为仓储后端时只改这一处地址与 AuthRepository 实现。
val wanandroidBaseUrl = providers.gradleProperty("WANANDROID_BASE_URL")
    .getOrElse("https://wanandroid.com/")
require(wanandroidBaseUrl.endsWith("/")) {
    "WANANDROID_BASE_URL must end with '/': $wanandroidBaseUrl"
}

android {
    namespace = "com.wangxingxing.wxxstoremanage"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.wangxingxing.wxxstoremanage"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.wangxingxing.wxxstoremanage.HiltTestRunner"
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
        buildConfigField("String", "WANANDROID_BASE_URL", "\"$wanandroidBaseUrl\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // 截图测试源集仍为实验特性，AGP 尚未提供稳定开关。
    experimentalProperties["android.experimental.enableScreenshotTest"] = true
    testOptions {
        unitTests {
            // Robolectric 需要读取资源与合并后的 manifest 才能渲染 Compose 页面。
            isIncludeAndroidResources = true
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Robolectric 默认在测试运行时自行下载 android-all-instrumented，每次换机器或代理拦截 TLS 都会失败；
// 改由 Gradle 解析并同步到固定目录，测试进程只读取本地 jar，保证离线可复现。
val robolectricDependencies = configurations.create("robolectricDependencies") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

val robolectricDependencyDirectory = layout.buildDirectory.dir("robolectric-deps")

val syncRobolectricDependencies = tasks.register<Sync>("syncRobolectricDependencies") {
    from(robolectricDependencies)
    into(robolectricDependencyDirectory)
}

dependencies {
    robolectricDependencies(libs.robolectric.android.all)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockwebserver3)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.robolectric)

    screenshotTestImplementation(platform(libs.androidx.compose.bom))
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.reqable.user.certificate.trust)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.withType<Test>().configureEach {
    dependsOn(syncRobolectricDependencies)
    systemProperty(
        "robolectric.dependency.dir",
        robolectricDependencyDirectory.get().asFile.absolutePath,
    )
    extensions.configure<JacocoTaskExtension> {
        // Robolectric 生成的类包含未定位的调试信息，排除后 Jacoco 才能正确插桩。
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "生成 debug 单元测试覆盖率报告"

    dependsOn("testDebugUnitTest")

    // 单元测试运行的是 AGP 经 ASM 转换后的类，报告必须使用同一份输出，否则 Jacoco 会判定类与执行数据不匹配。
    val debugClasses = layout.buildDirectory.dir(
        "intermediates/classes/debug/transformDebugClassesWithAsm/dirs",
    )
    classDirectories.setFrom(
        debugClasses.map { directory ->
            directory.asFileTree.matching {
                exclude(
                    "**/R.class",
                    "**/R$*.class",
                    "**/BuildConfig.class",
                    "**/Hilt_*.class",
                    "**/*_HiltModules*.class",
                    "**/*_GeneratedInjector.class",
                    "**/*_Factory.class",
                    "**/*_MembersInjector.class",
                    "**/*_Impl.class",
                    "**/dagger/**",
                    "**/hilt_aggregated_deps/**",
                )
            }
        },
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


