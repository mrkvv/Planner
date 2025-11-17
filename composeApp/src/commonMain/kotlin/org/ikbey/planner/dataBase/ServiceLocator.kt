package org.ikbey.planner.dataBase

/** Класс для работы со всеми данными приложения. Обеспечивает единственность экзмелпяров всех связанных классов:
 * - [LocalDatabaseManager] - класс для работы с локальной БД (SQLDelight);
 * - [SupabaseRepository] - класс для работы с облачной БД (supabase);
 * - [SyncManager] - класс для синхронизации работы двух классов выше.
 *
 * #### Использование:
 * В любом месте пишем
 * - val localdb = ServiceLocator.localDatabaseManager, если нам нужен доступ к локальной БД;
 * - val syncManager = ServiceLocator.syncManager, если нужны settings или
 * если потребовалось гдето впендырить доп синхронизацию
 * - ServiceLocator.supabaseRepository врядли понадобится, нужны будут только localdb и для настроек syncManager
 * */
object ServiceLocator {
    private var _localDatabaseManager: LocalDatabaseManager? = null

    val localDatabaseManager: LocalDatabaseManager
        get() = _localDatabaseManager ?: LocalDatabaseManager(DatabaseFactory.createDatabase()).also {
            _localDatabaseManager = it
        }

    val supabaseRepository: SupabaseRepository by lazy {
        SupabaseRepository()
    }

    val syncManager: SyncManager by lazy {
        SyncManager(localDatabaseManager, supabaseRepository)
    }

    fun setLocalDb(localdb: LocalDatabaseManager) {
        _localDatabaseManager = localdb
    }
}