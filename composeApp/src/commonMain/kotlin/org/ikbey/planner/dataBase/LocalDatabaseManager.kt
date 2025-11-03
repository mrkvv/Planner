package org.ikbey.planner.dataBase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.ikbey.planner.localDB.LocalDatabase

/** Класс для работы с локальной базой данных.
 * Абсолютно все методы этого класса можно найти в SQL нотации
 * в *composeApp\src\commonMain\sqldelight\org\ikbey\planner\localDB\LocalDatabase.sq* */
class LocalDatabaseManager(private val database: LocalDatabase) {

    private val query = database.localDatabaseQueries


    // ---------------- ИНСТИТУТЫ ------------------

    suspend fun getFaculties(): List<Faculty> {
        return withContext(Dispatchers.IO) {
            query.selectAllFaculties().executeAsList().map { faculty ->
                Faculty(
                    id = faculty.id.toInt(),
                    name = faculty.name,
                    abbr = faculty.abbr
                )
            }
        }
    }

    suspend fun insertFaculty(faculty: Faculty) {
        withContext(Dispatchers.IO) {
            query.insertFaculty(
                id = faculty.id.toLong(),
                name = faculty.name,
                abbr = faculty.abbr
            )
        }
    }

    suspend fun deleteAllFaculties() {
        withContext(Dispatchers.IO) {
            query.deleteAllFaculties()
        }
    }

    // ---------------- ГРУППЫ ------------------

    suspend fun getGroups(): List<Group> {
        return withContext(Dispatchers.IO) {
            query.selectAllGroups().executeAsList().map { group ->
                Group(
                    id = group.id.toInt(),
                    faculty_id = group.faculty_id.toInt(),
                    name = group.name
                )
            }
        }
    }

    suspend fun getGroupsByFaculty(facultyId: Int): List<Group> {
        return withContext(Dispatchers.IO) {
            query.selectGroupsByFaculty(facultyId.toLong()).executeAsList().map { group ->
                Group(
                    id = group.id.toInt(),
                    faculty_id = group.faculty_id.toInt(),
                    name = group.name
                )
            }
        }
    }

    suspend fun insertGroup(group: Group) {
        withContext(Dispatchers.IO) {
            query.insertGroup(
                id = group.id.toLong(),
                faculty_id = group.faculty_id.toLong(),
                name = group.name
            )
        }
    }

    suspend fun deleteAllGroups() {
        withContext(Dispatchers.IO) {
            query.deleteAllGroups()
        }
    }

    // ---------------- КАЛЕНДАРЬ ИВЕНТС ------------------
    suspend fun getCalendarEvents(): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            query.selectAllCalendarEvents().executeAsList().map { event ->
                CalendarEvent(
                    id = event.id.toInt(),
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    start_time = event.start_time,
                    end_time = event.end_time,
                    location = event.location,
                    creator = event.creator,
                    calendar_name = event.calendar_name
                )
            }
        }
    }

    suspend fun getCalendarEventsByDate(date: String): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            query.selectCalendarEventsByDate(date).executeAsList().map { event ->
                CalendarEvent(
                    id = event.id.toInt(),
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    start_time = event.start_time,
                    end_time = event.end_time,
                    location = event.location,
                    creator = event.creator,
                    calendar_name = event.calendar_name
                )
            }
        }
    }

    suspend fun getCalendarEventsByDateRange(startDate: String, endDate: String): List<CalendarEvent> {
        return withContext(Dispatchers.IO) {
            query.selectCalendarEventsByDateRange(startDate, endDate).executeAsList().map { event ->
                CalendarEvent(
                    id = event.id.toInt(),
                    title = event.title,
                    description = event.description,
                    date = event.date,
                    start_time = event.start_time,
                    end_time = event.end_time,
                    location = event.location,
                    creator = event.creator,
                    calendar_name = event.calendar_name
                )
            }
        }
    }

    suspend fun insertCalendarEvent(event: CalendarEvent) {
        withContext(Dispatchers.IO) {
            query.insertCalendarEvent(
                id = event.id.toLong(),
                title = event.title,
                description = event.description,
                date = event.date,
                start_time = event.start_time,
                end_time = event.end_time,
                location = event.location,
                creator = event.creator,
                calendar_name = event.calendar_name
            )
        }
    }

    suspend fun deleteCalendarEvent(id: Int) {
        withContext(Dispatchers.IO) {
            query.deleteCalendarEvent(id.toLong())
        }
    }

    suspend fun deleteAllCalendarEvents() {
        withContext(Dispatchers.IO) {
            query.deleteAllCalendarEvents()
        }
    }

    // ---------------- РАСПИСАНИЕ ПОЛЬЗОВАТЕЛЯ (и только) ------------------
    suspend fun getUserSchedule(): List<Schedule> {
        return withContext(Dispatchers.IO) {
            query.selectAllUserSchedule().executeAsList().map { schedule ->
                Schedule(
                    id = schedule.id.toInt(),
                    group_id = schedule.group_id.toInt(),
                    date = schedule.date,
                    weekday = schedule.weekday.toInt(),
                    subject = schedule.subject,
                    type = schedule.type,
                    start_time = schedule.start_time,
                    end_time = schedule.end_time,
                    teacher = schedule.teacher,
                    audithory = schedule.audithory
                )
            }
        }
    }

    suspend fun getUserScheduleByDate(date: String): List<Schedule> {
        return withContext(Dispatchers.IO) {
            query.selectUserScheduleByDate(date).executeAsList().map { schedule ->
                Schedule(
                    id = schedule.id.toInt(),
                    group_id = schedule.group_id.toInt(),
                    date = schedule.date,
                    weekday = schedule.weekday.toInt(),
                    subject = schedule.subject,
                    type = schedule.type,
                    start_time = schedule.start_time,
                    end_time = schedule.end_time,
                    teacher = schedule.teacher,
                    audithory = schedule.audithory
                )
            }
        }
    }

    suspend fun getUserScheduleByDateRange(startDate: String, endDate: String): List<Schedule> {
        return withContext(Dispatchers.IO) {
            query.selectUserScheduleByDateRange(startDate, endDate).executeAsList().map { schedule ->
                Schedule(
                    id = schedule.id.toInt(),
                    group_id = schedule.group_id.toInt(),
                    date = schedule.date,
                    weekday = schedule.weekday.toInt(),
                    subject = schedule.subject,
                    type = schedule.type,
                    start_time = schedule.start_time,
                    end_time = schedule.end_time,
                    teacher = schedule.teacher,
                    audithory = schedule.audithory
                )
            }
        }
    }

    suspend fun insertUserSchedule(schedule: Schedule) {
        withContext(Dispatchers.IO) {
            query.insertUserSchedule(
                id = schedule.id.toLong(),
                group_id = schedule.group_id.toLong(),
                date = schedule.date,
                weekday = schedule.weekday.toLong(),
                subject = schedule.subject,
                type = schedule.type,
                start_time = schedule.start_time,
                end_time = schedule.end_time,
                teacher = schedule.teacher,
                audithory = schedule.audithory
            )
        }
    }

    suspend fun deleteUserSchedule() {
        withContext(Dispatchers.IO) {
            query.deleteUserSchedule()
        }
    }

    // ---------------- ЗАМЕТКИ ПОЛЬЗОВАТЕЛЯ (и только) ------------------
    suspend fun getAllUserNotes(): List<Note> {
        return withContext(Dispatchers.IO) {
            query.selectAllUserNotes().executeAsList().map { note ->
                Note(
                    id = note.id.toInt(),
                    lesson_id = note.lesson_id?.toInt(),
                    date = note.date,
                    header = note.header_,
                    note = note.note,
                    is_notifications_enabled = note.is_notifications_enabled?.toInt() != 0,
                    created_at = note.created_at,
                    updated_at = note.updated_at
                )
            }
        }
    }

    suspend fun getUserNotesByLesson(lessonId: Int): List<Note> {
        return withContext(Dispatchers.IO) {
            query.selectUserNotesByLesson(lessonId.toLong()).executeAsList().map { note ->
                Note(
                    id = note.id.toInt(),
                    lesson_id = note.lesson_id?.toInt(),
                    date = note.date,
                    header = note.header_,
                    note = note.note,
                    is_notifications_enabled = note.is_notifications_enabled?.toInt() != 0,
                    created_at = note.created_at,
                    updated_at = note.updated_at
                )
            }
        }
    }

    suspend fun getUserNotesByDate(date: String): List<Note> {
        return withContext(Dispatchers.IO) {
            query.selectUserNotesByDate(date).executeAsList().map { note ->
                Note(
                    id = note.id.toInt(),
                    lesson_id = note.lesson_id?.toInt(),
                    date = note.date,
                    header = note.header_,
                    note = note.note,
                    is_notifications_enabled = note.is_notifications_enabled?.toInt() != 0,
                    created_at = note.created_at,
                    updated_at = note.updated_at
                )
            }
        }
    }

    suspend fun insertUserNote(note: Note) {
        withContext(Dispatchers.IO) {
            query.insertUserNote(
                id = note.id.toLong(),
                lesson_id = note.lesson_id?.toLong(),
                date = note.date,
                header_ = note.header,
                note = note.note,
                is_notifications_enabled = if (note.is_notifications_enabled == true) 1L else 0L,
                created_at = note.created_at,
                updated_at = note.updated_at
            )
        }
    }

    suspend fun deleteUserNote(id: Int) {
        withContext(Dispatchers.IO) {
            query.deleteUserNote(id.toLong())
        }
    }

    suspend fun deleteAllUserNotes() {
        withContext(Dispatchers.IO) {
            query.deleteAllUserNotes()
        }
    }

    // ---------------- STICKY NOTES ------------------
    suspend fun getAllStickyNotes(): List<StickyNote> {
        return withContext(Dispatchers.IO) {
            query.selectAllUserStickyNotes().executeAsList().map { note ->
                StickyNote(
                    id = note.id.toInt(),
                    header = note.header_,
                    note = note.note
                )
            }
        }
    }

    suspend fun getStickyNoteById(id: Int): List<StickyNote> {
        return withContext(Dispatchers.IO) {
            query.getUserStickyNoteById(id.toLong()).executeAsList().map { note ->
                StickyNote(
                    id = note.id.toInt(),
                    header = note.header_,
                    note = note.note
                )
            }
        }
    }

    /** Обновляет, если существует такой id, иначе создает новую */
    suspend fun updateStickyNote(stickyNote: StickyNote) {
        withContext(Dispatchers.IO) {
            query.insertOrReplaceStickyNote(
                id = stickyNote.id.toLong(),
                header_ = stickyNote.header,
                note = stickyNote.note,
            )
        }
    }

    /** Создает новую с autoincrement id */
    suspend fun insertStickyNote(stickyNote: StickyNote) {
        withContext(Dispatchers.IO) {
            query.insertStickyNote(
                header_ = stickyNote.header,
                note = stickyNote.note,
            )
        }
    }

    suspend fun deleteStickyNote(id: Int) {
        withContext(Dispatchers.IO) {
            query.deleteStickyNote(id.toLong())
        }
    }

    suspend fun deleteAllStickyNotes() {
        withContext(Dispatchers.IO) {
            query.deleteAllStickyNotes()
        }
    }

    // ---------------- НАСТРОЙКИ ПОЛЬЗОВАТЕЛЯ (и только) ------------------

    /** Вариант функции "черновой".
     * Позволяет прям насильно получить любое значение из таблицы настроек по ключу.
     *
     * ВАЖНО!: лучше использовать специализированные функции из SyncManager:
     * - [SyncManager.getLastSyncTime]
     * - [SyncManager.getGroupId]
     *
     * Возможные варианты ключей, если ну прям очень хочется юзнуть эту функцию:
     * @param last_sync_time timestamp последней синхронизации пользователя
     * @param group_id id группы пользователя (не name группы, а именно id!)
     *
     * В случае, если использовать другое значение для ключа,
     * то вероятно вернется null (если кто-то до вас не установил значение такому ключу, что маловероятно) */
    suspend fun getSetting(key: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                query.getSetting(key).executeAsOneOrNull()?.value_  //тоже хз почему _ - мб просто имя зарезервировано гдето там, поэтому..
            } catch (e: Exception) {
                println("Ошибка в LocalDatabaseManager.getSetting: ${e.message}")
                null
            }
        }
    }

    /** Вариант функции "черновой".
     * Позволяет прям насильно установить любое значение существующему ключу или создать новый ключ с таким значением.
     *
     * ВАЖНО!: лучше использовать специализированные функции из SyncManager
     * - [SyncManager.setLastSyncTime]
     * - [SyncManager.setGroupId]
     *
     * Возможные варианты ключей, если ну прям очень хочется юзнуть эту функцию:
     * @param last_sync_time timestamp последней синхронизации пользователя
     * @param group_id id группы пользователя (не name группы, а именно id!)
     *
     * В случае, если использовать другое значение для ключа,
     * то создатся новая запись в локальной БД с указанным ключем и значением. */
    suspend fun setSetting(key: String, value: String) {
        withContext(Dispatchers.IO) {
            query.setSetting(key, value)
        }
    }

    /** Полностью удаляет строку с указанным ключем из таблицы настроек.
     * Вполне достаточно юзать:
     * - [SyncManager.clearLastSyncTime] */
    suspend fun deleteSetting(key: String) {
        withContext(Dispatchers.IO) {
            query.deleteSetting(key)
        }
    }
}
