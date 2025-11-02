package org.ikbey.planner.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import org.ikbey.planner.*
import org.ikbey.planner.dataBase.ServiceLocator
import org.ikbey.planner.dataBase.StickyNote
import org.ikbey.planner.getInterFont

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
                modifier = Modifier.padding(top = 30.dp),
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

        StickyNotesArea(
            modifier = Modifier.padding(top = 20.dp, start = 25.dp, end = 25.dp)
        )
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
            text = num?.toString() ?: "",
            fontFamily = if (isToday) getInterFont(InterFontType.EXTRA_BOLD)
                else getInterFont(InterFontType.REGULAR),
            fontSize = if (isToday) 22.sp else 20.sp,
            color = if (isToday) DarkOrange else DarkGreen
        )
    }

}

@Composable
fun StickyNotesArea(
    modifier: Modifier
) {
    val localdb = ServiceLocator.localDatabaseManager
    val stickyNotes = remember { mutableStateListOf<StickyNote>() }

    var showStickyNotePage by remember { mutableStateOf(false) }
    var chosenStickyNote by remember { mutableStateOf<StickyNote?>(null) }
    var isListChanged by remember { mutableStateOf(false) }

    LaunchedEffect(Unit, isListChanged) {
        val notes = localdb.getAllStickyNotes()
        stickyNotes.clear()
        stickyNotes.addAll(notes)
        isListChanged = false
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stickyNotes) { note ->
            StickyNoteElement(
                modifier = Modifier,
                stickyNote = note,
                onStickyNoteClick = {
                    showStickyNotePage = true
                    chosenStickyNote = note
                }
            )
        }
        item {
            StickyNoteAddingButton(
                modifier = Modifier.padding(bottom = 26.dp),
                onClick = { showStickyNotePage = true }
            )
        }
    }

    if(showStickyNotePage) {
        StickyNotePage(
            stickyNote = chosenStickyNote,
            onDismissRequest = {
                showStickyNotePage = false
                chosenStickyNote = null
            },
            onChangeInStickyNotesList = { isListChanged = true }
        )
    }
}

@Composable
fun StickyNoteElement(
    modifier: Modifier,
    stickyNote: StickyNote,
    onStickyNoteClick: () -> Unit
) {
    Box (
        modifier = modifier.aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onStickyNoteClick
            ),
        contentAlignment = Alignment.Center
    ){
        Icon(
            imageVector = getIcon(IconType.LIL_STICK),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = stickyNote.header,
            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
            color = Brown,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun StickyNoteAddingButton(
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier.aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getIcon(IconType.LIL_STICK),
            contentDescription = "",
            tint = Color.Unspecified,
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = "Добавить заметку...",
            fontFamily = getInterFont(InterFontType.REGULAR),
            color = DarkOrange,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun StickyNotePage(
    stickyNote: StickyNote?,
    onDismissRequest: () -> Unit,
    onChangeInStickyNotesList: () -> Unit
) {
    var header by remember { mutableStateOf(stickyNote?.header ?: "") }
    var note by remember { mutableStateOf(stickyNote?.note ?: "") }
    val localdb = ServiceLocator.localDatabaseManager
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = {
            coroutineScope.launch {
                val newStickyNote = StickyNote(stickyNote?.id ?: -1, header, note)
                if (stickyNote != null) { //если это существующая заметка, то обновляем
                    localdb.updateStickyNote(newStickyNote)
                    onChangeInStickyNotesList()
                } else if (header.isNotBlank()) { //если новая и чтото есть, то вставляем
                    localdb.insertStickyNote(newStickyNote)
                    onChangeInStickyNotesList()
                }
                onDismissRequest()
            }
        }
    ) {
        Box(
            modifier = Modifier.aspectRatio(1f)
        ) {
            Icon(
                imageVector = getIcon(IconType.BIG_STICK),
                contentDescription = "",
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )

            Column (modifier = Modifier.padding(18.dp)) {
                BasicTextField(
                    value = header,
                    onValueChange = { header = it },
                    textStyle = TextStyle(
                        fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                        color = Brown,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 30.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            innerTextField()
                            if (header.isEmpty()) {
                                Text(
                                    text = "Добавить заголовок",
                                    fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                                    color = Brown,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                )

                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    textStyle = TextStyle(
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        color = DarkOrange,
                        fontSize = 20.sp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 5.dp, bottom = 40.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            innerTextField()
                            if (note.isEmpty()) {
                                Text(
                                    text = "Добавить описание",
                                    fontFamily = getInterFont(InterFontType.REGULAR),
                                    color = DarkOrange,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                )
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .padding(18.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    imageVector = getIcon(IconType.TRASH),
                    contentDescription = "Удалить sticky note",
                    tint = Color.Unspecified,
                    modifier = Modifier.clickable{
                        coroutineScope.launch {
                            if (stickyNote != null) {
                                localdb.deleteStickyNote(stickyNote.id)
                                onChangeInStickyNotesList()
                            }
                            onDismissRequest()
                        }
                    }
                )
            }
        }
    }
}
