package org.ikbey.planner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ikbey.planner.*
import org.ikbey.planner.getInterFont
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun MonthScreenPreview() {
    MonthScreen( onBackClick = {} )
}

@Composable
fun MonthScreen(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Yellow)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var showSettings by remember {mutableStateOf(false)}

        val calendarManager = remember { CalendarManager() }
        var year by remember { mutableStateOf(calendarManager.getCurrentYear()) }
        var month by remember { mutableStateOf(calendarManager.getCurrentMonth()) }

        Box(
            modifier = Modifier.fillMaxWidth()
                .systemBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            //Центрированная плашка с месяцем
            CalendarHeader(
                modifier = Modifier.padding(top = 60.dp),
                calendarManager = calendarManager,
                year = year,
                month = month,
                onHeaderClick = {
                    month = calendarManager.getCurrentMonth()
                    year = calendarManager.getCurrentYear()
                }
            )
            //Кнопка настроек оверлапом в правый верх
            Box(
                modifier = Modifier.fillMaxWidth()
                    .padding(end = 18.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                SettingsButton {
                    showSettings = !showSettings
                }
            }
        }

        CalendarWindow(
            modifier = Modifier.padding(top = 16.dp),
            calendarManager = calendarManager,
            year = year,
            month = month,
            onMonthChangeUp = {
                if (month == 12) {
                    year++
                    month = 1
                }
                else month++
            },
            onMonthChangeDown = {
                if (month == 1) {
                    year--
                    month = 12
                }
                else month--
            }
        )
        // TODO: Стики ноутс допилить, когда будет иконка
    }
}

@Composable
fun CalendarHeader(
    modifier: Modifier,
    calendarManager: CalendarManager,
    year: Int,
    month: Int,
    onHeaderClick: () -> Unit
) {
    var monthName by remember(month) {
        mutableStateOf(calendarManager.getMonthName(month))
    }

    Row (
        modifier = modifier
            .clip(shape = RoundedCornerShape(15.dp))
            .clickable(onClick = onHeaderClick)
            .background(color = LightOrange)
            .padding(vertical = 6.dp, horizontal = 12.dp)
    ) {
        Text(
            text = monthName,
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = DarkOrange
        )
        Text(
            text = " • ",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = DarkOrange
        )
        Text(
            text = year.toString(),
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = DarkOrange
        )
    }
}

@Composable
fun CalendarWindow(
    modifier: Modifier,
    calendarManager: CalendarManager,
    year: Int,
    month: Int,
    onMonthChangeUp: () -> Unit,
    onMonthChangeDown: () -> Unit
) {
    var calendarMatrix by remember (year, month) {
        mutableStateOf(calendarManager.getCalendarMatrix(year, month))
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .clickable(
                    onClick = onMonthChangeDown,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = "<",
                fontFamily = getInterFont(InterFontType.LIGHT),
                fontSize = 32.sp,
                color = DarkGreen
            )
        }

        Column (
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
                .background(color = LightGreen)
                .weight(1f)
        ) {
            calendarMatrix.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    week.forEach { day ->
                        CalendarElement(
                            modifier = Modifier.weight(1f),
                            calendarManager = calendarManager,
                            year = year,
                            month = month,
                            num = day
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .height(200.dp)
                .clickable(
                    onClick = onMonthChangeUp,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 4.dp),
                text = ">",
                fontFamily = getInterFont(InterFontType.LIGHT),
                fontSize = 32.sp,
                color = DarkGreen
            )
        }
    }
}

@Composable
fun CalendarElement(
    modifier: Modifier,
    calendarManager: CalendarManager,
    year: Int,
    month: Int,
    num: Int?
) {
    val isToday = calendarManager.isToday(year, month, num)
    Box(
        modifier = modifier.aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                color = if (isToday) LightOrange
                        else Color.Unspecified
            )
            .clickable(
                enabled = num != null,
                onClick = {

                }
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = modifier,
            text = if (num!= null) num.toString() else "",
            fontFamily = if (isToday) getInterFont(InterFontType.EXTRA_BOLD)
                else getInterFont(InterFontType.REGULAR),
            fontSize = if (isToday) 22.sp else 20.sp,
            color = if (isToday) DarkOrange else DarkGreen
        )
    }

}
