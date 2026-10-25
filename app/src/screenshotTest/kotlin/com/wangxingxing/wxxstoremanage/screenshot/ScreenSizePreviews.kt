/**
 * author : 王星星
 * date : 2026-10-25 17:10
 * email : 1099420259@qq.com
 * description : 截图测试共用的窗口尺寸多预览注解；九种组合对应紧凑、中等与扩展宽度。
 */
package com.wangxingxing.wxxstoremanage.screenshot

import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "400x400", widthDp = 400, heightDp = 400, showBackground = true)
@Preview(name = "400x500", widthDp = 400, heightDp = 500, showBackground = true)
@Preview(name = "400x1000", widthDp = 400, heightDp = 1000, showBackground = true)
@Preview(name = "610x400", widthDp = 610, heightDp = 400, showBackground = true)
@Preview(name = "610x500", widthDp = 610, heightDp = 500, showBackground = true)
@Preview(name = "610x1000", widthDp = 610, heightDp = 1000, showBackground = true)
@Preview(name = "900x400", widthDp = 900, heightDp = 400, showBackground = true)
@Preview(name = "900x500", widthDp = 900, heightDp = 500, showBackground = true)
@Preview(name = "900x1000", widthDp = 900, heightDp = 1000, showBackground = true)
annotation class ScreenSizePreviews
