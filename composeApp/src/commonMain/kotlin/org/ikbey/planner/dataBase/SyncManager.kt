package org.ikbey.planner.dataBase

import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

/** Класс для синхронизации работы облачной и локальных БД.
 * Обновляет данные автоматически, если пользователь открыл приложение и до этого не открывал его больше 24 часов. */
class SyncManager(
    private val localDb: LocalDatabaseManager,
    private val remoteDb: SupabaseRepository,

) {
    companion object {
        private val SYNC_INTERVAL = 24.toDuration(DurationUnit.HOURS)
        private val LAST_SYNC_TIME_DB_KEY = "last_sync_time"
        private val GROUP_ID_DB_KEY = "group_id"
        private val INIT_LOAD = "init_load"
    }
    suspend fun getLastSyncTime(): Long? {
        return localDb.getSetting(LAST_SYNC_TIME_DB_KEY)?.toLongOrNull()
    }

    suspend fun setLastSyncTime(timestamp: Long) {
        localDb.setSetting(LAST_SYNC_TIME_DB_KEY, timestamp.toString())
    }

    suspend fun clearLastSyncTime() {
        localDb.deleteSetting(LAST_SYNC_TIME_DB_KEY)
    }

    suspend fun getGroupId(): String? {
        return localDb.getSetting(GROUP_ID_DB_KEY)
    }

    /** Любая смена группы = полная синхронизация */
    suspend fun setGroupId(groupId: Int) {
        localDb.setSetting(GROUP_ID_DB_KEY, groupId.toString())
        forceSync()
    }

    /** Полная актуализация локальной БД в соответствии с удаленной */
    private suspend fun performFullSync() {
        try {
            val remoteCalendarEvents = remoteDb.getAllCalendarEvents()
            localDb.deleteAllCalendarEvents()
            remoteCalendarEvents.forEach { localDb.insertCalendarEvent(it) }

            val groupId = getGroupId()
            val remoteSchedule = remoteDb.getScheduleByGroup(groupId?.toInt() ?: 0)
            localDb.deleteUserSchedule()
            remoteSchedule.forEach { localDb.insertUserSchedule(it) }
        } catch (e: Exception) {
            println("Ошибка в SyncManager.performFullSync(): ${e.message}")
            throw e
        }
    }

    /** Загрузка институтов и групп */
    private suspend fun performInitSync() {
        try {
            val remoteFaculties = remoteDb.getAllFaculties()
            localDb.deleteAllFaculties()
            remoteFaculties.forEach { localDb.insertFaculty(it) }

            localDb.deleteAllGroups()
            remoteFaculties.forEach { faculty ->
                val groupsForFaculty = remoteDb.getAllGroupsByFaculty(faculty.id)
                groupsForFaculty.forEach { group ->
                    localDb.insertGroup(group)
                }
            }
            localDb.setSetting(INIT_LOAD, "1")
        } catch (e: Exception) {
            println("Ошибка в SyncManager.performInitSync(): ${e.message}")
            throw e
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun forceSync() {
        performFullSync()
        setLastSyncTime(Clock.System.now().toEpochMilliseconds())
    }

    @OptIn(ExperimentalTime::class)
    suspend fun syncIfNeeded(): Boolean {
        val lastSync = getLastSyncTime()
        val now = Clock.System.now().toEpochMilliseconds()

        return if (getGroupId() == null && localDb.getSetting(INIT_LOAD) != "1") {
            performInitSync()
            true
        }
        else if (getGroupId() != null && (lastSync == null || (now - lastSync) > SYNC_INTERVAL.inWholeMilliseconds) ) {
            performFullSync()
            setLastSyncTime(now)
            true
        } else false
    }
}
