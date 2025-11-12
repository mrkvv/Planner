@file:OptIn(ExperimentalTestApi::class)

package org.ikbey.planner.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import org.ikbey.planner.CalendarManager
import kotlin.test.Test

class UIMonthScreenTest {
    val calendarManager = CalendarManager()

    @Test
    fun calendarHeaderTest() = runComposeUiTest {
        setContent {
            CalendarHeader(
                modifier = Modifier.Companion,
                calendarManager = calendarManager,
                year = 2005,
                month = 12,
                onHeaderClick = { }
            )
        }

        onNodeWithText("Декабрь").assertExists()
        onNodeWithText("2005").assertExists()
    }


}