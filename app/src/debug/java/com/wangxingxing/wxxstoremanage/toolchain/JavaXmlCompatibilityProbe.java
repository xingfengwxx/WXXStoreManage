/**
 * author : 王星星
 * date : 2026-10-26 13:36
 * email : 1099420259@qq.com
 * description : Java/XML 编译能力探针，仅存在于 debug 源集，由 JavaXmlCompatibilityProbeTest 调用并 inflate 布局；注意：这是构建能力回归门禁，不是业务代码，删除或移动会让手写 Java 与 layout 编译能力的验证失效。
 */
package com.wangxingxing.wxxstoremanage.toolchain;

import com.wangxingxing.wxxstoremanage.R;

public final class JavaXmlCompatibilityProbe {

    private JavaXmlCompatibilityProbe() {
    }

    public static int layoutResId() {
        return R.layout.toolchain_compat_probe;
    }
}
