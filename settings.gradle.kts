pluginManagement {
    // 阿里云镜像在 GitHub Actions 的海外运行器上会返回 502，Gradle 的插件解析会把这类仓库故障
    // 吞成“找不到插件”，因此 CI 只走官方仓库，本地保留镜像加速。
    val useAliyunMirrors = System.getenv("CI").isNullOrBlank()
    repositories {
        // 阿里云镜像对 AGP 与 aapt2 存在同步延迟，官方仓库必须排在镜像之前。
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        if (useAliyunMirrors) {
            maven("https://maven.aliyun.com/repository/google") {
                content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
            maven("https://maven.aliyun.com/repository/gradle-plugin")
            maven("https://maven.aliyun.com/repository/public")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    // 判断条件与 pluginManagement 一致：CI 不访问阿里云镜像。
    val useAliyunMirrors = System.getenv("CI").isNullOrBlank()
    repositories {
        // 阿里云镜像对 AGP 与 aapt2 存在同步延迟，官方仓库必须排在镜像之前。
        google()
        if (useAliyunMirrors) {
            maven("https://maven.aliyun.com/repository/google") {
                content {
                    includeGroupByRegex("com\\.android.*")
                    includeGroupByRegex("com\\.google.*")
                    includeGroupByRegex("androidx.*")
                }
            }
            maven("https://maven.aliyun.com/repository/public")
        }
        mavenCentral()
    }
}

rootProject.name = "WXXStoreManage"
include(":app")
 