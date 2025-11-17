package org.ikbey.planner.dataBase

import assertk.assertThat
import assertk.assertions.isTrue
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotEquals

class SupabaseRepositoryTest {
    private val supabaseRepository = SupabaseRepository()

    @Test
    fun testGetAllFaculties() = runTest {
        val faculties = supabaseRepository.getAllFaculties()

        assertNotEquals(0, faculties.size)

        assertThat(faculties.any {
            it.id == 125 &&
                    it.name == "Институт компьютерных наук и кибербезопасности" &&
                    it.abbr == "ИКНК"
        }).isTrue()
    }

    @Test
    fun testGetAllGroupsByFaculty() = runTest {
        val groupsIknk = supabaseRepository.getAllGroupsByFaculty(125)

        assertNotEquals(0, groupsIknk.size)

        assertThat(groupsIknk.any {
            it.id == 42799 && it.faculty_id == 125 && it.name == "5130904/30107"
        }).isTrue()
    }

    @Test
    fun testGetScheduleByGroup() = runTest {
        val schedule = supabaseRepository.getScheduleByGroup(42799)

        assertNotEquals(0, schedule.size)

        assertThat(schedule.any {
            it.teacher == "Коликова Татьяна Всеволодовна" && it.type == "Практика"
        }).isTrue()
    }

    @Test
    fun testGetAllCalendarEvents() = runTest {
        val calEvents = supabaseRepository.getAllCalendarEvents()

        assertNotEquals(0, calEvents.size)

        assertThat(calEvents.any {
            it.calendar_name == "ПРОФ.ИКНК"
        }).isTrue()
    }
}
