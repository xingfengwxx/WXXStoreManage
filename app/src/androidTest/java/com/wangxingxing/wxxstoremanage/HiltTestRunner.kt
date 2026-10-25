/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 仪器测试专用 runner，用 HiltTestApplication 替换正式 Application 以安装测试组件与依赖替身。
 */
package com.wangxingxing.wxxstoremanage

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
