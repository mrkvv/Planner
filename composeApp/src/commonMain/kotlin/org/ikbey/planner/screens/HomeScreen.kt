package org.ikbey.planner.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ikbey.planner.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.ikbey.planner.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ikbey.planner.dataBase.*


@Composable
fun HomeScreen(
    onMonthClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAddNoteClick: (NoteData) -> Unit
) {
    val calendarManager = remember { CalendarManager() }
    val currentDate = PlatformDate()
    val localDb = ServiceLocator.localDatabaseManager
    val coroutineScope = rememberCoroutineScope()

    var selectedYear by remember { mutableStateOf(currentDate.year) }
    var selectedMonth by remember { mutableStateOf(currentDate.month) }
    var selectedDay by remember { mutableStateOf(currentDate.day) }
    var showAddNoteSheet by remember { mutableStateOf(false) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var isListChanged by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(selectedYear, selectedMonth, selectedDay, isListChanged) {
        try {
            val date = formatDate(selectedYear, selectedMonth, selectedDay)
            val loadedNotes = localDb.getUserNotesByDate(date)
            notes = loadedNotes
        } catch (e: Exception) {
            println("ERROR: Ошибка загрузки заметок: ${e.message}")
        }
    }

    val bottomSheetOffset by animateDpAsState(
        targetValue = if (showAddNoteSheet) 0.dp else 500.dp,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    val showTopGradient by remember(scrollState.value) {
        derivedStateOf { scrollState.value > 0 }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Yellow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 45.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                MonthText(
                    year = selectedYear,
                    month = selectedMonth,
                    calendarManager = calendarManager,
                    modifier = Modifier.align(Alignment.Bottom)
                )

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = LightOrange,
                            shape = CircleShape
                        )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            DaysScrollList(
                year = selectedYear,
                month = selectedMonth,
                selectedDay = selectedDay,
                onDayClick = { day ->
                    selectedDay = day
                    isListChanged = !isListChanged
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayWeekText(
                    year = selectedYear,
                    month = selectedMonth,
                    day = selectedDay,
                    calendarManager = calendarManager
                )
                TodayBox(
                    isToday = calendarManager.isToday(selectedYear, selectedMonth, selectedDay)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Область для отображения заметок с градиентным оверлеем
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NotesSection(
                    notes = notes,
                    scrollState = scrollState,
                    onNoteClick = { note ->
                        selectedNote = note
                        showNoteDetail = true
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                )

                // Градиентный оверлей (только при скролле)
                if (showTopGradient) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Yellow, Color.Transparent),
                                    startY = 0f,
                                    endY = Float.POSITIVE_INFINITY
                                )
                            )
                            .align(Alignment.TopStart)
                    )
                }

                // Градиентный оверлей снизу (всегда)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Yellow),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                        .align(Alignment.BottomStart)
                )
            }
        }

        AddButton(
            onClick = { showAddNoteSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }

    if (showAddNoteSheet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable { showAddNoteSheet = false }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom
            ) {
                BottomSheetMenu(
                    onDismiss = { showAddNoteSheet = false },
                    onMonthClick = onMonthClick,
                    onEventsClick = onEventsClick,
                    onAddNoteClick = { noteData ->
                        coroutineScope.launch {
                            try {
                                val date = formatDate(selectedYear, selectedMonth, selectedDay)
                                val noteWithDate = noteData.copy(date = date)
                                val note = noteWithDate.toNote()
                                localDb.insertUserNote(note)
                                isListChanged = !isListChanged
                            } catch (e: Exception) {
                                println("ERROR: Ошибка при добавлении заметки: ${e.message}")
                            }
                        }
                        showAddNoteSheet = false
                    },
                    modifier = Modifier.offset(y = bottomSheetOffset)
                )
            }
        }
    }

    if (showNoteDetail && selectedNote != null) {
        NoteDetailDialog(
            note = selectedNote!!,
            onDismiss = {
                showNoteDetail = false
                selectedNote = null
            },
            onDelete = {
                coroutineScope.launch {
                    try {
                        localDb.deleteUserNote(selectedNote!!.id)
                        isListChanged = !isListChanged
                        showNoteDetail = false
                        selectedNote = null
                    } catch (e: Exception) {
                        println("ERROR: Ошибка при удалении заметки: ${e.message}")
                    }
                }
            },
            onUpdate = { updatedNote ->
                coroutineScope.launch {
                    try {
                        localDb.insertUserNote(updatedNote)
                        isListChanged = !isListChanged
                    } catch (e: Exception) {
                        println("ERROR: Ошибка при обновлении заметки: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun NotesSection(
    notes: List<Note>,
    scrollState: ScrollState,
    onNoteClick: (Note) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (notes.isNotEmpty()) {
            notes.forEach { note ->
                NoteCard(
                    note = note,
                    onNoteClick = { onNoteClick(note) }
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        color = Yellow,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Сегодня дел нет!",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = DarkOrange.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun NoteCard(
    note: Note,
    onNoteClick: () -> Unit
) {
    var isCompleted by remember { mutableStateOf(false) }
    val cardColor = if (isCompleted) LightGreen else Orange
    val textColor = if (isCompleted) DarkGreen else Black
    val buttonColor = if (isCompleted) DarkGreen else Color.Transparent
    val buttonBorderColor = DarkGreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 400.dp)
            .background(
                color = cardColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .clickable { onNoteClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 30.dp)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .align(Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = note.start_time ?: "",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = textColor
                )
                if (!note.end_time.isNullOrEmpty()) {
                    Text(
                        text = note.end_time,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        color = textColor
                    )
                }
            }

            Text(
                text = "•",
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .align(Alignment.CenterVertically),
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = textColor
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
                    .align(Alignment.CenterVertically)
            ) {
                val (headerText, bodyText) = getHeaderAndBody(note)

                if (headerText.isNotEmpty()) {
                    Text(
                        text = headerText,
                        fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                        fontSize = 20.sp,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (bodyText.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                if (bodyText.isNotEmpty()) {
                    Text(
                        text = bodyText,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        color = textColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (headerText.isEmpty() && bodyText.isEmpty()) {
                    Spacer(modifier = Modifier.height(20.sp.value.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .size(25.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-5).dp)
                .clickable {
                    isCompleted = !isCompleted
                }
                .background(
                    color = buttonColor,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = buttonBorderColor,
                    shape = CircleShape
                )
        )
    }
}

private fun getHeaderAndBody(note: Note): Pair<String, String> {
    // Если есть отдельный заголовок в note.header и текст в note.note
    if (!note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
        return note.header to note.note
    }
    // Если есть только заголовок
    else if (!note.header.isNullOrEmpty() && note.note.isNullOrEmpty()) {
        return note.header to ""
    }
    // Если есть только текст (note.note)
    else if (note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
        val lines = note.note.split('\n')
        return when {
            lines.size == 1 -> {
                // Если одна строка - весь текст идет в заголовок
                note.note to ""
            }
            lines.size >= 2 -> {
                // Первая строка - заголовок, остальное - текст
                val header = lines[0]
                val body = lines.subList(1, lines.size).joinToString("\n")
                header to body
            }
            else -> "" to ""
        }
    }
    // Если нет ничего
    else {
        return "" to ""
    }
}

@Composable
fun NoteDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Note) -> Unit
) {
    val localDb = ServiceLocator.localDatabaseManager
    val coroutineScope = rememberCoroutineScope()

    var startTime by remember { mutableStateOf(note.start_time ?: "") }
    var endTime by remember { mutableStateOf(note.end_time ?: "") }
    var location by remember { mutableStateOf(note.header ?: "") }
    var noteText by remember { mutableStateOf(
        if (!note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
            "${note.header}\n${note.note}"
        } else {
            note.header ?: note.note ?: ""
        }
    ) }
    var isInterval by remember { mutableStateOf(!note.end_time.isNullOrEmpty()) }
    var isNotification by remember { mutableStateOf(note.is_notifications_enabled == true) }
    var hasChanges by remember { mutableStateOf(false) }

    val saveChanges = {
        if (hasChanges) {
            coroutineScope.launch {
                val updatedNote = createUpdatedNote(note, startTime, endTime, location, noteText, isInterval, isNotification)
                onUpdate(updatedNote)
            }
        }
    }

    Dialog(
        onDismissRequest = {
            saveChanges()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
                .background(
                    color = LightGreen,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Время",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 24.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(20.dp))

                UnifiedTimeInputField(
                    startTime = startTime,
                    endTime = endTime,
                    isInterval = isInterval,
                    onStartTimeChange = {
                        startTime = it
                        hasChanges = true
                    },
                    onEndTimeChange = {
                        endTime = it
                        hasChanges = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Интервал",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = DarkGreen
                )

                Spacer(modifier = Modifier.width(14.dp))

                Switch(
                    checked = isInterval,
                    onCheckedChange = {
                        isInterval = it
                        hasChanges = true
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkGreen,
                        checkedTrackColor = SwitchGreen,
                        uncheckedThumbColor = DarkGreen,
                        uncheckedTrackColor = LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Заметка",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "Место",
                    modifier = Modifier
                        .padding(horizontal = 10.dp),
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = DarkGreen
                )

                Spacer(modifier = Modifier.width(10.dp))

                SimpleLocationField(
                    value = location,
                    onValueChange = {
                        location = it
                        hasChanges = true
                    },
                    modifier = Modifier.fillMaxWidth(0.9f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SimpleInputField(
                value = noteText,
                onValueChange = {
                    noteText = it
                    hasChanges = true
                },
                placeholder = "Заголовок",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(250.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isNotification,
                        onCheckedChange = {
                            isNotification = it
                            hasChanges = true
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkGreen,
                            checkedTrackColor = SwitchGreen,
                            uncheckedThumbColor = DarkGreen,
                            uncheckedTrackColor = LightGray
                        )
                    )
                    Text(
                        "Уведомление",
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        color = DarkGreen
                    )
                }
                Icon(
                    imageVector = Icons.trash,
                    contentDescription = "Удалить заметку",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onDelete() }
                        .padding(8.dp),
                    tint = DarkGreen
                )
            }
        }
    }
}

private fun createUpdatedNote(
    originalNote: Note,
    startTime: String,
    endTime: String,
    location: String,
    noteText: String,
    isInterval: Boolean,
    isNotification: Boolean
): Note {
    val lines = noteText.split('\n')
    val (header, body) = when {
        lines.size == 1 -> {
            noteText to ""
        }
        lines.size >= 2 -> {
            val headerText = lines[0]
            val bodyText = lines.subList(1, lines.size).joinToString("\n")
            headerText to bodyText
        }
        else -> "" to ""
    }

    return originalNote.copy(
        start_time = startTime,
        end_time = if (isInterval && endTime.isNotEmpty()) endTime else null,
        header = if (location.isNotEmpty()) location else header,
        note = body,
        is_notifications_enabled = isNotification
    )
}


@Composable
fun MonthText(
    year: Int,
    month: Int,
    calendarManager: CalendarManager,
    modifier: Modifier = Modifier
) {
    val monthName = calendarManager.getMonthName(month)

    Text(
        text = monthName,
        modifier = modifier,
        textAlign = TextAlign.Start,
        fontFamily = getInterFont(InterFontType.REGULAR),
        fontSize = 26.sp
    )
}


@Composable
fun DaysScrollList(
    year: Int,
    month: Int,
    selectedDay: Int,
    onDayClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val calendarManager = remember { CalendarManager() }
    val daysInMonth = calendarManager.getDaysAmountInMonth(year, month)
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(selectedDay) {
        delay(100)
        val itemWidthWithSpacing = with(density) { 46.dp.roundToPx() }
        val scrollPosition = (selectedDay - 1) * itemWidthWithSpacing
        val adjustedPosition = scrollPosition - with(density) { 100.dp.roundToPx() }
        scrollState.animateScrollTo(adjustedPosition.coerceAtLeast(0))
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (day in 1..daysInMonth) {
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
            fontFamily = if (isSelected) getInterFont(InterFontType.EXTRA_BOLD) else getInterFont(InterFontType.REGULAR),
            fontSize = 24.sp,
            color = textColor
        )
    }
}

@Composable
fun DayWeekText(
    year: Int,
    month: Int,
    day: Int,
    calendarManager: CalendarManager,
    modifier: Modifier = Modifier
) {
    val dayOfWeek = calendarManager.getDayOfWeekName(calendarManager.calculateDayOfWeek(year, month, day))

    Text(
        text = "$dayOfWeek, $day",
        modifier = modifier,
        textAlign = TextAlign.Start,
        fontFamily = getInterFont(InterFontType.REGULAR),
        fontSize = 24.sp
    )
}

@Composable
fun TodayBox(isToday: Boolean) {
    if (isToday) {
        Box(
            modifier = Modifier
                .background(
                    color = LightOrange,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 10.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Сегодня",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = DarkOrange
            )
        }
    }
}



@Composable
fun AddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = DarkGreen,
        shape = CircleShape,
        modifier = modifier.size(70.dp)
    ) {
        Icon(
            imageVector = Icons.plus,
            contentDescription = "+",
            tint = White,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun BottomSheetMenu(
    onDismiss: () -> Unit,
    onMonthClick: () -> Unit,
    onEventsClick: () -> Unit,
    onAddNoteClick: (NoteData) -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isInterval by remember { mutableStateOf(false) }
    var isNotification by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .background(
                color = LightGreen,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Время",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(20.dp))

            UnifiedTimeInputField(
                startTime = startTime,
                endTime = endTime,
                isInterval = isInterval,
                onStartTimeChange = { startTime = it },
                onEndTimeChange = { endTime = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Интервал",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.width(14.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Заметка",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 26.dp),
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 24.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Место",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = DarkGreen
            )

            Spacer(modifier = Modifier.width(10.dp))

            SimpleLocationField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(0.9f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SimpleInputField(
            value = note,
            onValueChange = { note = it },
            placeholder = "Заголовок",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(160.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                    fontFamily = getInterFont(InterFontType.REGULAR),
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
                    .heightIn(min = 45.dp)
                    .widthIn(min = 120.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LightOrange),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    "Добавить",
                    fontFamily = getInterFont(InterFontType.BOLD),
                    fontSize = 20.sp,
                    color = DarkOrange,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun SimpleLocationField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = Color.Black

            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        "Место проведения",
                        color = LightGray,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                innerTextField()
            }
        )
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
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isInterval) {
            Row(
                modifier = Modifier
                    .widthIn(min = 200.dp, max = 250.dp)
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
                    placeholder = "__:__",
                    modifier = Modifier.width(70.dp)
                )

                Text("-", color = LightGray, fontFamily = getInterFont(InterFontType.SEMI_BOLD), fontSize = 16.sp)

                IntervalTimePart(
                    value = endTime,
                    onValueChange = onEndTimeChange,
                    placeholder = "__:__",
                    modifier = Modifier.width(70.dp)
                )
            }
        } else {
            SingleTimeField(
                value = startTime,
                onValueChange = onStartTimeChange,
                modifier = Modifier
                    .width(100.dp)
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
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(
            text = value,
            selection = TextRange(value.length)
        ))
    }

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
            value = textFieldValue,
            onValueChange = { newValue ->
                val formatted = delayedTimeFormat(newValue.text)

                textFieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                )
                onValueChange(formatted)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = DarkOrange
            )
        )

        if (value.isEmpty()) {
            Text(
                "__:__",
                color = LightGray,
                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun IntervalTimePart(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Transparent)
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                val formatted = delayedTimeFormat(newValue.text)

                textFieldValue = TextFieldValue(
                    text = formatted,
                    selection = TextRange(formatted.length)
                )
                onValueChange(formatted)
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                color = DarkOrange
            )
        )

        if (value.isEmpty()) {
            Text(
                placeholder,
                color = LightGray,
                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun delayedTimeFormat(input: String): String {
    val digitsOnly = input.filter { it.isDigit() }
    val result = StringBuilder()

    for (i in digitsOnly.indices) {
        when (i) {
            0 -> {
                val digit = digitsOnly[i].toString().toInt()
                if (digit in 0..2) result.append(digit)
            }
            1 -> {
                val firstDigit = digitsOnly[0].toString().toInt()
                val secondDigit = digitsOnly[i].toString().toInt()
                if (firstDigit == 2) {
                    if (secondDigit in 0..3) result.append(secondDigit)
                } else {
                    if (secondDigit in 0..9) result.append(secondDigit)
                }
            }
            2 -> {
                val digit = digitsOnly[i].toString().toInt()
                if (digit in 0..5) result.append(digit)
            }
            3 -> {
                val digit = digitsOnly[i].toString().toInt()
                if (digit in 0..9) result.append(digit)
            }
        }
    }

    val formatted = result.toString()

    return when (formatted.length) {
        4 -> "${formatted.take(2)}:${formatted.takeLast(2)}"
        else -> formatted
    }
}

@Composable
fun SimpleInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var header by remember {
        mutableStateOf(value.split('\n').firstOrNull() ?: "")
    }
    var body by remember {
        mutableStateOf(
            if (value.contains('\n')) value.substringAfter('\n') else ""
        )
    }

    LaunchedEffect(header, body) {
        val newValue = if (body.isNotEmpty()) "$header\n$body" else header
        onValueChange(newValue)
    }

    Box(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            BasicTextField(
                value = header,
                onValueChange = { newHeader ->
                    val filteredHeader = newHeader.replace("\n", "")
                    header = filteredHeader
                },
                textStyle = TextStyle(
                    fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                    color = Color.Black,
                    fontSize = 24.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                decorationBox = { innerTextField ->
                    Box {
                        innerTextField()
                        if (header.isEmpty() && body.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                                color = LightGray,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            )

            BasicTextField(
                value = body,
                onValueChange = { newBody ->
                    body = newBody
                },
                textStyle = TextStyle(
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    color = Color.Black,
                    fontSize = 20.sp
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                decorationBox = { innerTextField ->
                    Box {
                        innerTextField()
                        if (body.isEmpty() && header.isEmpty()) {
                            Text(
                                text = "Введите текст...",
                                fontFamily = getInterFont(InterFontType.REGULAR),
                                color = LightGray,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            )
        }
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