/**
 * author : 王星星
 * date : 2026-10-26 13:36
 * email : 1099420259@qq.com
 * description : 验证 Kotlin 测试可以调用 debug 源集的手写 Java 类并 inflate debug layout，守住 Java/XML 编译能力；注意：测试失败通常意味着源码集被排除、资源编译被关闭或 Java 编译路径被破坏。
 */
package com.wangxingxing.wxxstoremanage.toolchain

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.wangxingxing.wxxstoremanage.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class JavaXmlCompatibilityProbeTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `java class compiles and layout inflates`() {
        assertEquals(R.layout.toolchain_compat_probe, JavaXmlCompatibilityProbe.layoutResId())

        val layout = LayoutInflater.from(context)
            .inflate(JavaXmlCompatibilityProbe.layoutResId(), null)

        val marker: View? = layout.findViewById(R.id.toolchain_compat_probe_marker)
        assertNotNull(marker)
    }
}
