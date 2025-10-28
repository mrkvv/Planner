package org.ikbey.planner.dataBase

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.ikbey.planner.localDB.LocalDatabase

/** Вспомогательный класс для получения контекста (андроид специфика).
 * Обязательно вызвать в MainActivity, без контекста в андроиде мы далеко не уйдем... */
object AndroidContext {
    lateinit var applicationContext: Context
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}

/** Android-реализация создания драйвера для локальной базы данных SQLdelight */
actual class DriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = LocalDatabase.Schema,
            context = AndroidContext.applicationContext,
            name = "local.db"
        )
    }
}
