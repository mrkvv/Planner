package org.ikbey.planner.screens

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ikbey.planner.*
import org.ikbey.planner.Icons
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.delay
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.ikbey.planner.dataBase.*
import org.ikbey.planner.notification.NotificationManager
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.testTag
import org.ikbey.planner.CalendarManager

@Composable
fun HomeScreen(
    notificationManager: NotificationManager,
    selectedYear: Int,
    selectedMonth: Int,
    selectedDay: Int,
    onDayChange: (day: Int) -> Unit,
    onSwipeToMonth: () -> Unit,
    onSwipeToEvents: () -> Unit
) {
    val calendarManager = remember { CalendarManager() }
    val currentDate = PlatformDate()
    val localDb = ServiceLocator.localDatabaseManager
    val coroutineScope = rememberCoroutineScope()
    val syncManager = ServiceLocator.syncManager

    var isLoading by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    var isLeftSwipeActive by remember { mutableStateOf(false) }
    var isRightSwipeActive by remember { mutableStateOf(false) }

    var showAddNoteSheet by remember { mutableStateOf(false) }
    var showNoteDetail by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var selectedNoteData by remember { mutableStateOf<NoteData?>(null) }
    var notes by remember { mutableStateOf<List<Note>>(emptyList()) }
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var calendarEvents by remember { mutableStateOf<List<CalendarEvent>>(emptyList()) }
    var isListChanged by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    var syncTrigger by remember { mutableStateOf(0) }
    var settingsCloseTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            val maxId = localDb.getMaxNoteId()
            initializeNoteIdCounter(maxId)

            val needsSync = syncManager.syncIfNeeded()
            if (needsSync) {
                delay(100)
            }
            isLoading = false
            syncTrigger++
        } catch (e: Exception) {
            println("ERROR: Failed to sync or initialize note ID counter: ${e.message}")
            isLoading = false
        }
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay, isListChanged, syncTrigger, settingsCloseTrigger) {
        try {
            val date = formatDate(selectedYear, selectedMonth, selectedDay)
            val loadedNotes = localDb.getUserNotesByDate(date)

            notes = loadedNotes.sortedWith { note1, note2 ->
                compareNotesAdvanced(note1, note2)
            }
        } catch (e: Exception) {
            println("ERROR: Ошибка загрузки заметок: ${e.message}")
        }
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay, syncTrigger, settingsCloseTrigger) {
        try {
            val date = formatDate(selectedYear, selectedMonth, selectedDay)
            val loadedSchedules = localDb.getUserScheduleByDate(date)
            schedules = loadedSchedules
        } catch (e: Exception) {
            println("ERROR: Ошибка загрузки расписания: ${e.message}")
        }
    }

    LaunchedEffect(selectedYear, selectedMonth, selectedDay, syncTrigger, settingsCloseTrigger) {
        try {
            val date = formatDate(selectedYear, selectedMonth, selectedDay)
            val loadedEvents = localDb.getTrackedCalendarEventsByDate(date)
            calendarEvents = loadedEvents
        } catch (e: Exception) {
            println("ERROR: Ошибка загрузки мероприятий: ${e.message}")
        }
    }

    val allItems = remember(notes, schedules, calendarEvents) {
        val userNotes = notes.map { note ->
            note.toNoteData() to note
        }
        val scheduleNotes = schedules.map { schedule ->
            schedule.toNoteData() to schedule.toNote()
        }
        val eventNotes = calendarEvents.map { event ->
            event.toNoteData() to event.toNote()
        }
        (userNotes + scheduleNotes + eventNotes).sortedBy { (noteData, _) ->
            timeToMinutes(noteData.startTime)
        }
    }

    val updateItemDoneState = { itemId: Int, isDone: Boolean, itemType: NoteType ->
        when (itemType) {
            NoteType.USER_NOTE -> {
                notes = notes.map { note ->
                    if (note.id == itemId) note.copy(is_done = isDone) else note
                }
            }
            NoteType.SCHEDULE -> {
                schedules = schedules.map { schedule ->
                    if (schedule.id == itemId) schedule.copy(is_done = isDone) else schedule
                }
            }
            NoteType.CALENDAR_EVENT -> {
                calendarEvents = calendarEvents.map { event ->
                    if (event.id == itemId) event.copy(is_done = isDone) else event
                }
            }
        }
    }

    val toggleNoteDone = { noteId: Int, isDone: Boolean, noteType: NoteType ->
        coroutineScope.launch {
            try {
                updateItemDoneState(noteId, isDone, noteType)

                when (noteType) {
                    NoteType.USER_NOTE -> {
                        localDb.updateUserNoteIsDone(noteId, isDone)
                    }
                    NoteType.SCHEDULE -> {
                        localDb.updateUserScheduleIsDone(noteId, isDone)
                    }
                    NoteType.CALENDAR_EVENT -> {
                        localDb.updateCalendarEventIsDone(noteId, isDone)
                    }
                }
            } catch (e: Exception) {
                println("ERROR: Ошибка при обновлении состояния: ${e.message}")
                updateItemDoneState(noteId, !isDone, noteType)
            }
        }
    }

    val onToggleNoteDone: (Int, Boolean, NoteType) -> Unit = { noteId, isDone, noteType ->
        toggleNoteDone(noteId, isDone, noteType)
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
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { change ->
                        val start = change.x
                        val screenWidth = size.width
                        isLeftSwipeActive = start < screenWidth * 0.40f
                        isRightSwipeActive = start > screenWidth * 0.60f
                    },
                    onHorizontalDrag = {change, dragAmount ->
                        if (isLeftSwipeActive && dragAmount > 50f){
                            onSwipeToMonth()
                        }
                        if (isRightSwipeActive && dragAmount < -50f){
                            onSwipeToEvents()
                        }
                    },

                    onDragEnd = {
                        isLeftSwipeActive = false
                        isRightSwipeActive = false
                    }
                )
            }
            .testTag("home-screen-container")
    ) {
        if (isLeftSwipeActive) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color.Gray)
            )
        }

        if (isRightSwipeActive) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(Color.Gray)
                    .align(Alignment.CenterEnd)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                MonthText(
                    year = selectedYear,
                    month = selectedMonth,
                    calendarManager = calendarManager,
                    modifier = Modifier.align(Alignment.Bottom)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = DarkGreen,
                                strokeWidth = 4.dp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                color = LightOrange,
                                shape = CircleShape
                            )
                    ) {
                        SettingsButton(
                            isSettingsOpen = showSettings,
                            onClick = {
                                if (!isLoading) {
                                    showSettings = true
                                }
                            }
                        )

                        if (showSettings) {
                            SettingsCard(
                                onDismiss = {
                                    showSettings = false
                                    settingsCloseTrigger++
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DaysScrollList(
                year = selectedYear,
                month = selectedMonth,
                selectedDay = selectedDay,
                onDayClick = { day ->
                    onDayChange(day)
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                NotesSection(
                    items = allItems,
                    scrollState = scrollState,
                    onNoteClick = {noteData, note ->
                        selectedNote = note
                        selectedNoteData = noteData
                        showNoteDetail = true
                    },
                    onToggleNoteDone = onToggleNoteDone,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp)
                )

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
                    onAddNoteClick = { noteData ->
                        coroutineScope.launch {
                            try {
                                val date = formatDate(selectedYear, selectedMonth, selectedDay)
                                val noteWithDate = noteData.copy(date = date)
                                val note = noteWithDate.toUserNote()

                                println("🔔 [DEBUG] Создаем заметку с уведомлением: ${noteData.isNotification}")
                                println("🔔 [DEBUG] Время: ${note.start_time}, Дата: ${note.date}")

                                localDb.insertUserNote(note)
                                isListChanged = !isListChanged

                                // Уведомления для пользовательских заметок
                                if (note.is_notifications_enabled == true) {
                                    println("🔔 [DEBUG] Вызываем scheduleNotification для заметки ${note.id}")
                                    notificationManager.scheduleNotification(note)
                                } else {
                                    println("🔔 [DEBUG] Уведомление отключено для заметки ${note.id}")
                                }
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

    if (showNoteDetail && selectedNote != null && selectedNoteData != null) {
        NoteDetailDialog(
            notificationManager = notificationManager, // Передаем менеджер уведомлений
            note = selectedNote!!,
            noteData = selectedNoteData!!,
            onDismiss = {
                showNoteDetail = false
                selectedNote = null
                selectedNoteData = null
            },
            onDelete = {
                if (selectedNoteData?.type == NoteType.USER_NOTE) {
                    coroutineScope.launch {
                        try {
                            // Отменяем уведомление при удалении
                            notificationManager.cancelNotification(selectedNote!!.id)
                            localDb.deleteUserNote(selectedNote!!.id)
                            isListChanged = !isListChanged
                            showNoteDetail = false
                            selectedNote = null
                            selectedNoteData = null
                        } catch (e: Exception) {
                            println("ERROR: Ошибка при удалении заметки: ${e.message}")
                        }
                    }
                } else {
                    showNoteDetail = false
                    selectedNote = null
                    selectedNoteData = null
                }
            },
            onUpdate = { updatedNote ->
                if (selectedNoteData?.type == NoteType.USER_NOTE) {
                    coroutineScope.launch {
                        try {
                            localDb.insertUserNote(updatedNote)
                            isListChanged = !isListChanged

                            // Обновляем уведомления
                            println("🔔 [DEBUG] Обновляем заметку ${updatedNote.id}")
                            println("🔔 [DEBUG] Уведомление включено: ${updatedNote.is_notifications_enabled}")

                            if (updatedNote.is_notifications_enabled == true) {
                                println("🔔 [DEBUG] Вызываем scheduleNotification для обновленной заметки ${updatedNote.id}")
                                notificationManager.scheduleNotification(updatedNote)
                            } else {
                                println("🔔 [DEBUG] Отменяем уведомление для заметки ${updatedNote.id}")
                                notificationManager.cancelNotification(updatedNote.id)
                            }
                        } catch (e: Exception) {
                            println("ERROR: Ошибка при обновлении заметки: ${e.message}")
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun NotesSection(
    items: List<Pair<NoteData, Note>>,
    scrollState: ScrollState,
    onNoteClick: (NoteData, Note) -> Unit,
    onToggleNoteDone: (Int, Boolean, NoteType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (items.isNotEmpty()) {
            items.forEach { (noteData, note) ->
                NoteCard(
                    note = note,
                    noteData = noteData,
                    onNoteClick = { onNoteClick(noteData, note) },
                    onToggleDone = { isDone ->
                        onToggleNoteDone(note.id, isDone, noteData.type)
                    }
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
    noteData: NoteData,
    onNoteClick: () -> Unit,
    onToggleDone: (Boolean) -> Unit
) {
    val isCompleted = note.is_done
    val cardColor = if (isCompleted) LightGreen else Orange
    val textColor = if (isCompleted) DarkGreen else Black
    val buttonColor = if (isCompleted) DarkGreen else Color.Transparent
    val buttonBorderColor = DarkGreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp, max = 400.dp)
            .clickable { onNoteClick() }
            .background(
                color = cardColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 10.dp, vertical = 16.dp)
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
                .testTag("toggle-done-${note.id}")
                .size(25.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-5).dp)
                .clip(CircleShape)
                .clickable {
                    onToggleDone(!isCompleted)
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

    if (!note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
        return note.header to note.note
    }

    else if (!note.header.isNullOrEmpty() && note.note.isNullOrEmpty()) {
        return note.header to ""
    }

    else if (note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
        val lines = note.note.split('\n')
        return when {
            lines.size == 1 -> {
                note.note to ""
            }
            lines.size >= 2 -> {
                val header = lines[0]
                val body = lines.subList(1, lines.size).joinToString("\n")
                header to body
            }
            else -> "" to ""
        }
    }

    else {
        return "" to ""
    }
}

@Composable
fun NoteDetailDialog(
    notificationManager: NotificationManager,
    note: Note,
    noteData: NoteData,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Note) -> Unit
) {
    val isEditable = noteData.type == NoteType.USER_NOTE

    if (!isEditable) {
        ReadOnlyNoteDetailDialog(
            note = note,
            noteData = noteData,
            onDismiss = onDismiss
        )
    } else {
        EditableNoteDetailDialog(
            notificationManager = notificationManager,
            note = note,
            onDismiss = onDismiss,
            onDelete = onDelete,
            onUpdate = onUpdate
        )
    }
}

@Composable
fun ReadOnlyNoteDetailDialog(
    note: Note,
    noteData: NoteData,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onDismiss() }
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = LightGreen,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (noteData.type) {
                            NoteType.SCHEDULE -> "Расписание"
                            NoteType.CALENDAR_EVENT -> "Мероприятие"
                            else -> "Заметка"
                        },
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 24.sp,
                        color = Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Время:",
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        fontSize = 20.sp,
                        color = DarkGreen
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (noteData.isInterval) {
                            "${noteData.startTime} - ${noteData.endTime}"
                        } else {
                            noteData.startTime
                        },
                        fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                        fontSize = 20.sp,
                        color = Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (noteData.location.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Место:",
                            fontFamily = getInterFont(InterFontType.REGULAR),
                            fontSize = 20.sp,
                            color = DarkGreen
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = noteData.location,
                            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                            fontSize = 20.sp,
                            color = Black
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .verticalScroll(scrollState)
                        .padding(12.dp)
                ) {
                    val (header, body) = getHeaderAndBody(note)

                    if (header.isNotEmpty()) {
                        Text(
                            text = header,
                            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                            fontSize = 20.sp,
                            color = Black,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (body.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (body.isNotEmpty()) {
                        Text(
                            text = body,
                            fontFamily = getInterFont(InterFontType.REGULAR),
                            fontSize = 20.sp,
                            color = Black,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditableNoteDetailDialog(
    notificationManager: NotificationManager,
    note: Note,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (Note) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val dialogFocusRequester = remember { FocusRequester() }

    val localDb = ServiceLocator.localDatabaseManager
    val coroutineScope = rememberCoroutineScope()

    var startTime by remember { mutableStateOf(note.start_time ?: "") }
    var endTime by remember { mutableStateOf(note.end_time ?: "") }
    var location by remember { mutableStateOf(note.place ?: "") }
    var noteText by remember {
        mutableStateOf(
            if (!note.header.isNullOrEmpty() && !note.note.isNullOrEmpty()) {
                "${note.header}\n${note.note}"
            } else {
                note.header ?: note.note ?: ""
            }
        )
    }
    var isInterval by remember { mutableStateOf(!note.end_time.isNullOrEmpty()) }
    var isNotification by remember { mutableStateOf(note.is_notifications_enabled == true) }

    var timeError by remember { mutableStateOf(false) }
    var noteError by remember { mutableStateOf(false) }

    val originalNote = remember { note }

    var intervalError by remember { mutableStateOf(false) }

    val canSaveChanges = remember(startTime, endTime, noteText, isInterval) {
        val isStartTimeValid = isValidTime(startTime)
        val isEndTimeValid = if (isInterval) isValidTime(endTime) else true
        val isIntervalValid = if (isInterval) isValidTimeInterval(startTime, endTime) else true

        isStartTimeValid && isEndTimeValid && isIntervalValid && noteText.isNotBlank()
    }

    LaunchedEffect(Unit) {
        delay(100)
        dialogFocusRequester.requestFocus()
    }

    val hideKeyboard: () -> Unit = {
        println("🔔 [DEBUG] Скрываем клавиатуру через FocusRequester")
        coroutineScope.launch {
            dialogFocusRequester.requestFocus()
            delay(10)
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val saveChangesIfValid = {
        if (canSaveChanges) {
            coroutineScope.launch {
                val updatedNote = createUpdatedNote(
                    originalNote,
                    startTime,
                    endTime,
                    location,
                    noteText,
                    isInterval,
                    isNotification
                )

                println("🔔 [DEBUG] Обновляем заметку ${updatedNote.id}")
                println("🔔 [DEBUG] Уведомление включено: $isNotification")

                onUpdate(updatedNote)

                if (updatedNote.is_notifications_enabled == true) {
                    println("🔔 [DEBUG] Вызываем scheduleNotification для обновленной заметки ${updatedNote.id}")
                    notificationManager.scheduleNotification(updatedNote)
                } else {
                    println("🔔 [DEBUG] Отменяем уведомление для заметки ${updatedNote.id}")
                    notificationManager.cancelNotification(updatedNote.id)
                }
                onDismiss()
            }
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = {
            hideKeyboard()
            saveChangesIfValid()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(560.dp)
                .background(
                    color = LightGreen,
                    shape = RoundedCornerShape(16.dp)
                )
                .focusRequester(dialogFocusRequester)
                .focusable()
                .clickable { hideKeyboard() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                                timeError =
                                    !isValidTime(it) || (isInterval && !isValidTime(endTime))
                                intervalError =
                                    isInterval && !isValidTimeInterval(startTime, endTime)
                            },
                            onEndTimeChange = {
                                endTime = it
                                timeError =
                                    !isValidTime(startTime) || (isInterval && !isValidTime(it))
                                intervalError =
                                    isInterval && !isValidTimeInterval(startTime, endTime)
                            },
                            modifier = Modifier.weight(1f).testTag("note-input-field")
                        )
                    }

                    if (timeError || intervalError) {
                        Text(
                            text = when {
                                intervalError -> "Некорректный интервал"
                                timeError && isInterval -> "Укажите время полностью"
                                else -> "Укажите время полностью"
                            },
                            color = DarkGreen,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 26.dp)
                                .padding(top = 4.dp)
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
                                if (it) {
                                    timeError = !isValidTime(startTime) || !isValidTime(endTime)
                                    intervalError = !isValidTimeInterval(startTime, endTime)
                                } else {
                                    timeError = !isValidTime(startTime)
                                    intervalError = false
                                }
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 26.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Заметка",
                            fontFamily = getInterFont(InterFontType.REGULAR),
                            fontSize = 24.sp,
                            color = Color.Black
                        )

                    }

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
                            onValueChange = { location = it },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    SimpleInputField(
                        value = noteText,
                        onValueChange = {
                            noteText = it
                            noteError = it.isBlank()
                        },
                        placeholder = "Заголовок",
                        modifier = Modifier
                            .testTag("note-input-field")
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

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onDelete() }
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.trash,
                                contentDescription = "Удалить заметку",
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete-note-button"),
                                tint = DarkGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

fun isValidTimeInterval(startTime: String, endTime: String): Boolean {
    if (!isValidTime(startTime) || !isValidTime(endTime)) return false

    val startMinutes = timeToMinutes(startTime)
    val endMinutes = timeToMinutes(endTime)

    return endMinutes > startMinutes
}

private fun timeToMinutes(time: String?): Int {
    if (time.isNullOrEmpty() || time.length != 5 || time[2] != ':') return Int.MAX_VALUE

    return try {
        val (hours, minutes) = time.split(":").map { it.toInt() }
        hours * 60 + minutes
    } catch (e: Exception) {
        println("ERROR: Invalid time format: $time, error: ${e.message}")
        Int.MAX_VALUE
    }
}

@VisibleForTesting
internal fun createUpdatedNote(
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
        place = location.ifEmpty { null },
        header = header.ifEmpty { null },
        note = body.ifEmpty { null },
        is_notifications_enabled = isNotification,
        is_done = originalNote.is_done
    )
}


@VisibleForTesting
internal fun compareNotesAdvanced(note1: Note, note2: Note): Int {
    val time1 = note1.start_time ?: ""
    val time2 = note2.start_time ?: ""

    val startTime1 = timeToMinutes(time1)
    val startTime2 = timeToMinutes(time2)

    if (startTime1 != startTime2) {
        return startTime1.compareTo(startTime2)
    }

    val hasInterval1 = !note1.end_time.isNullOrEmpty()
    val hasInterval2 = !note2.end_time.isNullOrEmpty()

    if (hasInterval1 != hasInterval2) {
        return if (!hasInterval1) -1 else 1
    }

    if (hasInterval1 && hasInterval2) {
        val endTime1 = timeToMinutes(note1.end_time)
        val endTime2 = timeToMinutes(note2.end_time)
        if (endTime1 != endTime2) {
            return endTime1.compareTo(endTime2)
        }
    }

    return note1.id.compareTo(note2.id)
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
    onAddNoteClick: (NoteData) -> Unit,
    modifier: Modifier = Modifier
) {
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isInterval by remember { mutableStateOf(false) }
    var isNotification by remember { mutableStateOf(false) }

    // Состояние для ошибок валидации
    var timeError by remember { mutableStateOf(false) }
    var noteError by remember { mutableStateOf(false) }
    var intervalError by remember { mutableStateOf(false) }

    // Проверяем, можно ли добавить заметку
    val canAddNote = remember(startTime, endTime, note, isInterval) {
        val isStartTimeValid = isValidTime(startTime)
        val isEndTimeValid = if (isInterval) isValidTime(endTime) else true
        val isIntervalValid = if (isInterval) isValidTimeInterval(startTime, endTime) else true

        isStartTimeValid && isEndTimeValid && isIntervalValid && note.isNotBlank()
    }

    // Для закрытия клавиатуры при нажатии на область BottomSheetMenu
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(
                color = LightGreen,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .clickable { focusManager.clearFocus() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp)
                .verticalScroll(rememberScrollState())
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
                        timeError = !isValidTime(it) || (isInterval && !isValidTime(endTime))
                        intervalError = isInterval && !isValidTimeInterval(startTime, endTime)
                    },
                    onEndTimeChange = {
                        endTime = it
                        timeError = !isValidTime(startTime) || (isInterval && !isValidTime(it))
                        intervalError = isInterval && !isValidTimeInterval(startTime, endTime)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            if (timeError || intervalError) {
                Text(
                    text = when {
                        intervalError -> "Некорректный интервал"
                        timeError && isInterval -> "Укажите время полностью"
                        else -> "Укажите время полностью"
                    },
                    color = DarkGreen,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp)
                        .padding(top = 4.dp)


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
                        if (it) {
                            timeError = !isValidTime(startTime) || !isValidTime(endTime)
                            intervalError = !isValidTimeInterval(startTime, endTime)
                        } else {
                            timeError = !isValidTime(startTime)
                            intervalError = false
                        }
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Заметка",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    "Место",
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
                onValueChange = {
                    note = it
                    noteError = it.isBlank()
                },
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
                        val hasTimeError = if (isInterval) {
                            !isValidTime(startTime) || !isValidTime(endTime)
                        } else {
                            !isValidTime(startTime)
                        }
                        val hasIntervalError = isInterval && !isValidTimeInterval(startTime, endTime)
                        val hasNoteError = note.isBlank()

                        timeError = hasTimeError
                        intervalError = hasIntervalError
                        noteError = hasNoteError

                        if (!hasTimeError && !hasIntervalError && !hasNoteError) {
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
                        }
                    },
                    modifier = Modifier
                        .heightIn(min = 45.dp)
                        .widthIn(min = 120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canAddNote) LightOrange else LightGray
                    ),
                    enabled = canAddNote,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        "Добавить",
                        fontFamily = getInterFont(InterFontType.BOLD),
                        fontSize = 20.sp,
                        color = if (canAddNote) DarkOrange else SwitchGrayContour,
                        maxLines = 1
                    )
                }

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
            textStyle = TextStyle(
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
            textStyle = TextStyle(
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
            textStyle = TextStyle(
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

fun isValidTime(time: String): Boolean {
    if (time.length != 5) return false
    if (time[2] != ':') return false

    val hours = time.substring(0, 2).toIntOrNull() ?: return false
    val minutes = time.substring(3, 5).toIntOrNull() ?: return false

    return hours in 0..23 && minutes in 0..59
}
