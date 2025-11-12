package org.ikbey.planner

import kotlin.test.*

class CalendarManagerTest {
    val manager = CalendarManager()

    @Test
    fun testGetMonthName() {
        assertEquals("Январь", manager.getMonthName(1))
        assertEquals("Февраль", manager.getMonthName(2))
        assertEquals("Март", manager.getMonthName(3))
        assertEquals("Апрель", manager.getMonthName(4))
        assertEquals("Май", manager.getMonthName(5))
        assertEquals("Июнь", manager.getMonthName(6))
        assertEquals("Июль", manager.getMonthName(7))
        assertEquals("Август", manager.getMonthName(8))
        assertEquals("Сентябрь", manager.getMonthName(9))
        assertEquals("Октябрь", manager.getMonthName(10))
        assertEquals("Ноябрь", manager.getMonthName(11))
        assertEquals("Декабрь", manager.getMonthName(12))
        assertEquals("", manager.getMonthName(-20))
    }

    @Test
    fun testGetDayOfWeekName() {
        assertEquals("Понедельник", manager.getDayOfWeekName(1))
        assertEquals("Вторник", manager.getDayOfWeekName(2))
        assertEquals("Среда", manager.getDayOfWeekName(3))
        assertEquals("Четверг", manager.getDayOfWeekName(4))
        assertEquals("Пятница", manager.getDayOfWeekName(5))
        assertEquals("Суббота", manager.getDayOfWeekName(6))
        assertEquals("Воскресенье", manager.getDayOfWeekName(7))
        assertEquals("", manager.getDayOfWeekName(20))
    }

    @Test
    fun testCalculateDayOfWeek() {
        assertEquals(2, manager.calculateDayOfWeek(2025, 11, 11))
        assertEquals(7, manager.calculateDayOfWeek(2032, 2, 15))
        assertEquals(1, manager.calculateDayOfWeek(2026, 3, 2))
        assertEquals(4, manager.calculateDayOfWeek(2005, 9, 1))
    }

}