/**
 * author : 王星星
 * date : 2026-10-25 14:40
 * email : 1099420259@qq.com
 * description : Hilt 应用入口，初始化依赖注入容器；需在 AndroidManifest 中注册。
 */
package com.wangxingxing.wxxstoremanage.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WxxStoreManageApplication : Application()
