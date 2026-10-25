/**
 * author : 王星星
 * date : 2026-10-25 16:32
 * email : 1099420259@qq.com
 * description : 迁移测试基线；当前只有 v1 schema，先验证导出的 schema 资产可被读取并完成建库校验。
 */
package com.wangxingxing.wxxstoremanage.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun schemaV1CanBeCreatedAndValidated() {
        helper.createDatabase(TEST_DATABASE, 1).close()

        helper.runMigrationsAndValidate(TEST_DATABASE, 1, true).close()
    }

    private companion object {
        const val TEST_DATABASE = "migration-test.db"
    }
}
