package org.ikbey.planner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ikbey.planner.*
import org.ikbey.planner.White
import org.jetbrains.compose.ui.tooling.preview.Preview

import androidx.compose.material3.Icon
import org.ikbey.planner.Icons

@Composable
fun HomeScreen(
    onMonthClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAddNoteClick: (NoteData) -> Unit
) {
    var selectedDay by remember { mutableStateOf(1) }
    var showBottomSheet by remember { mutableStateOf(false) }
    //var showBottomSheet = true

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Yellow)
    ) {
        MonthText()
        DaysScrollList(
            selectedDay = selectedDay,
            onDayClick = { day -> selectedDay = day }
        )
        DayWeekText()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp, start = 290.dp)
        ) {
            TodayBox()
        }


        AddButton { showBottomSheet = true }


        if (showBottomSheet) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                BottomSheetMenu(
                    onDismiss = { showBottomSheet = false },
                    onMonthClick = onMonthClick,
                    onEventsClick = onEventsClick,
                    onAddNoteClick = onAddNoteClick
                )
            }
        }
    }
}

@Composable
fun MonthText() {
    Text(
        text = "Сентябрь",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 25.dp),
        textAlign = TextAlign.Start,
        fontSize = 26.sp
    )
}

@Composable
fun DaysScrollList(selectedDay: Int = 1, onDayClick: (Int) -> Unit = {}) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 110.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (day in 1..30) {
            val isSelected = day == selectedDay
            DayItem(
                day = day,
                isSelected = isSelected,
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
fun DayItem(day: Int, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    val backgroundColor = if (isSelected) LightOrange else Color.Transparent
    val textColor = if (isSelected) DarkOrange else Color.Black

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(15.dp))
            .width(40.dp)
            .height(45.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontSize = 24.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
    }
}

@Composable
fun DayWeekText() {
    Text(
        text = "Понедельник, 1",
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 180.dp, start = 25.dp),
        textAlign = TextAlign.Start,
        fontSize = 24.sp
    )
}

@Composable
fun TodayBox() {
    Box(
        modifier = Modifier
            .width(103.dp)
            .height(28.dp)
            .background(
                color = LightOrange,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(horizontal = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Сегодня",
            fontSize = 20.sp,
            color = DarkOrange
        )
    }
}

@Composable
fun AddButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = DarkGreen,
            shape = CircleShape,
            modifier = Modifier.size(70.dp)
        ) {
            Text(
                text = "+",
                fontSize = 60.sp,
                color = White
            )
        }
    }
}

@Composable
fun BottomSheetMenu(
    onDismiss: () -> Unit,
    onMonthClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAddNoteClick: (NoteData) -> Unit
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isInterval by remember { mutableStateOf(false) }
    var isNotification by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(480.dp)
            .background(
                color = LightGreen,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(horizontal = 24.dp)
    ) {

        UnifiedTimeInputField(
            startTime = startTime,
            endTime = endTime,
            isInterval = isInterval,
            onStartTimeChange = { startTime = it },
            onEndTimeChange = { endTime = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Интервал",
                modifier = Modifier
                    .padding(top = 10.dp, start = 190.dp),
                fontSize = 20.sp,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.width(16.dp))

            Switch(
                checked = isInterval,
                onCheckedChange = { isInterval = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkGreen,
                    checkedTrackColor = SwitchGreen,
                    uncheckedThumbColor = DarkGreen,
                    uncheckedTrackColor = LightGray
                )
            )
        }

        Text(
            text = "Заметка",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            fontSize = 24.sp
        )

        LocationField(
            value = location,
            onValueChange = { location = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )

        SimpleInputField(
            value = note,
            onValueChange = { note = it },
            placeholder = "Заголовок",
            modifier = Modifier
                .padding(top = 20.dp)
                .width(370.dp)
                .height(160.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Switch(
                    checked = isNotification,
                    onCheckedChange = { isNotification = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkGreen,
                        checkedTrackColor = SwitchGreen,
                        uncheckedThumbColor = DarkGreen,
                        uncheckedTrackColor = LightGray
                    )
                )
                Text(
                    "Уведомление",
                    fontSize = 20.sp,
                    color = DarkGreen
                )
            }

            Button(
                onClick = {
                    val noteData = NoteData(
                        startTime = startTime,
                        endTime = if (isInterval) endTime else "",
                        location = location,
                        note = note,
                        isInterval = isInterval,
                        isNotification = isNotification
                    )
                    onAddNoteClick(noteData)
                    onDismiss()
                },
                modifier = Modifier
                    .width(135.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightOrange),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    "Добавить",
                    fontSize = 20.sp,
                    color = DarkOrange
                )
            }
        }
    }
}

@Composable
fun UnifiedTimeInputField(
    startTime: String,
    endTime: String,
    isInterval: Boolean,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Время",
            fontSize = 24.sp
        )

        if (isInterval) {
            Row(
                modifier = Modifier
                    .width(170.dp)
                    .height(35.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IntervalTimePart(
                    value = startTime,
                    onValueChange = onStartTimeChange,
                    placeholder = "__:__"
                )

                Text("—", color = LightGray, fontSize = 20.sp)

                IntervalTimePart(
                    value = endTime,
                    onValueChange = onEndTimeChange,
                    placeholder = "__:__"
                )
            }
        } else {

            SingleTimeField(
                value = startTime,
                onValueChange = onStartTimeChange,
                modifier = Modifier
                    .width(105.dp)
                    .height(35.dp)
            )
        }
    }
}

@Composable
fun SingleTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newText ->
                val formatted = formatTimeInput(newText)
                if (formatted.length <= 5) {
                    onValueChange(formatted)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        "__:__",
                        color = LightGray,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun IntervalTimePart(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Box(
        modifier = Modifier
            .width(65.dp)
            .fillMaxHeight()
            .background(
                color = Color.Transparent
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = { newText ->
                val formatted = formatTimeInput(newText)
                if (formatted.length <= 5) {
                    onValueChange(formatted)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = Color.Black
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        color = LightGray,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

fun formatTimeInput(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    val limitedDigits = digitsOnly.take(4)

    return when {
        limitedDigits.isEmpty() -> ""
        limitedDigits.length <= 2 -> limitedDigits
        else -> {
            val hours = limitedDigits.take(2)
            val minutes = limitedDigits.drop(2)

            val validHours = hours.toIntOrNull()?.coerceIn(0, 23)?.toString()?.padStart(2, '0') ?: hours
            val validMinutes = minutes.toIntOrNull()?.coerceIn(0, 59)?.toString()?.padStart(2, '0') ?: minutes
            "$validHours:$validMinutes"
        }
    }
}


@Composable
fun LocationField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Место",
            fontSize = 20.sp,
            color = DarkGreen,
        )

        Box(
            modifier = Modifier
                .width(235.dp)
                .height(35.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = Color.Black
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            "Место проведения",
                            color = LightGray,
                            fontSize = 20.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
fun SimpleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxSize()
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                color = Color.Black
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && !isFocused) {
                        Column {
                            Text(
                                placeholder,
                                modifier = Modifier.padding(top = 5.dp, start = 5.dp),
                                color = LightGray,
                                fontSize = 24.sp
                            )
                            Text(
                                "Введите текст...",
                                modifier = Modifier.padding(top = 4.dp, start = 5.dp),
                                color = LightGray,
                                fontSize = 20.sp
                            )
                        }
                    } else {
                        Column {
                            if (value.isNotEmpty()) {
                                val firstLine = value.split('\n').firstOrNull() ?: ""
                                if (firstLine.isNotEmpty()) {
                                    Text(
                                        firstLine,
                                        color = Color.Black,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                                val remainingText = value.substring(firstLine.length).trimStart('\n')
                                if (remainingText.isNotEmpty()) {
                                    Text(
                                        remainingText,
                                        color = Color.Black,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                            Box(modifier = Modifier.alpha(0f)) {
                                innerTextField()
                            }
                        }
                    }
                    if (value.isEmpty() && isFocused) {
                        innerTextField()
                    }
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onMonthClick = {},
        onEventsClick = {},
        onAddNoteClick = {}
    )
}