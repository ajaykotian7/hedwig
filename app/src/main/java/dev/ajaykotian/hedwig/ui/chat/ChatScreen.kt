package dev.ajaykotian.hedwig.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.Message
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.formatDayHeader
import dev.ajaykotian.hedwig.ui.base.rememberTimeFormat
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.HedwigTopAppBarDefaults
import dev.ajaykotian.hedwig.ui.components.LightStatusBarIcons
import dev.ajaykotian.hedwig.ui.components.Shimmer
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun ChatScreen(
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = screenViewModel { getChatViewModel() },
) {
    LightStatusBarIcons()

    // "Visible" means resumed: going home, switching apps or turning the screen off all count as leaving.
    LifecycleResumeEffect(viewModel) {
        viewModel.onScreenVisible()
        onPauseOrDispose { viewModel.onScreenHidden() }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChatContent(
        uiState = uiState,
        messageText = viewModel.messageText,
        onMessageTextChange = viewModel::onMessageTextChange,
        onSendClick = viewModel::sendMessage,
        onBackClick = onBackClick,
        onWaveClick = viewModel::sendWave,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatContent(
    uiState: ChatUIState,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    onWaveClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    if (uiState.isLoading) {
                        TopBarShimmer()
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(name = uiState.otherUser.name, id = uiState.otherUser.uid, size = 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = uiState.otherUser.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (uiState.otherUser.about.isNotBlank()) {
                                    Text(
                                        text = uiState.otherUser.about,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                                        color = LocalContentColor.current.copy(alpha = 0.75f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = HedwigTopAppBarDefaults.colors(),
            )
        },
        bottomBar = {
            Column(
                // Lift the bar above the gesture bar, and above the keyboard when it's open.
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                uiState.errorMessage?.let {
                    ErrorPill(it, Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 2.dp))
                }
                MessageInput(
                    text = messageText,
                    onTextChange = onMessageTextChange,
                    onSendClick = onSendClick,
                )
            }
        },
        containerColor = HedwigTheme.colors.chatBackground,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { innerPadding ->
        when {
            uiState.isLoading -> MessageListShimmer(contentPadding = innerPadding)

            uiState.entries.isEmpty() -> EmptyChat(
                name = uiState.otherUser.name,
                onWaveClick = onWaveClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> MessageList(entries = uiState.entries, contentPadding = innerPadding)
        }
    }
}

@Composable
private fun EmptyChat(
    name: String,
    onWaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HedwigTheme.colors
    Box(
        modifier = modifier.padding(start = 40.dp, end = 40.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 280.dp) // order matters: cap first, then fill up to the cap
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = colors.incomingBubble,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shadowElevation = 1.dp,
        ) {
            Column(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "No messages yet",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 26.sp, fontWeight = FontWeight.ExtraBold),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Say hi to ${name.ifBlank { "them" }} to start the conversation.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 21.sp),
                    color = colors.secondaryText,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                // The tap target: the circle and its label together, with 8dp of padding around them.
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClickLabel = "Send a wave", role = Role.Button, onClick = onWaveClick)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(colors.chatBackground, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("👋", fontSize = 52.sp, lineHeight = 60.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Tap to wave",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                        color = colors.accentText,
                    )
                }
            }
        }
    }
}


@Composable
private fun MessageList(
    entries: List<ChatEntry>,
    contentPadding: PaddingValues,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Reversed list: item 0 is the newest. Index > 0 means it's scrolled fully out of view.
    val showJump by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    // Scroll to the latest only if the user is not already on some particular message
    val newest = entries.lastOrNull() as? ChatEntry.MessageItem
    LaunchedEffect(newest?.message?.id) {
        if (newest != null && (newest.isMine || listState.firstVisibleItemIndex <= 1)) {
            listState.animateScrollToItem(0)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true, //Latest on the bottom
            contentPadding = listPadding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // Newest first to match the reversed layout. asReversed() is a view, not a copy.
            items(
                items = entries.asReversed(),
                key = { it.key },
                contentType = { it::class }, // headers are only recycled as headers, bubbles as bubbles
            ) { entry ->
                when (entry) {
                    is ChatEntry.DayHeader -> DayHeader(entry.date)
                    is ChatEntry.MessageItem -> MessageBubble(entry)
                }
            }
        }
        AnimatedVisibility(
            visible = showJump,
            enter = fadeIn() + scaleIn(initialScale = 0.6f),
            exit = fadeOut() + scaleOut(targetScale = 0.6f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = contentPadding.calculateBottomPadding() + 12.dp),
        ) {
            SmallFloatingActionButton(
                onClick = { coroutineScope.launch { listState.animateScrollToItem(0) } },
                shape = CircleShape,
                containerColor = HedwigTheme.colors.incomingBubble,
                contentColor = HedwigTheme.colors.secondaryText,
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Jump to newest message")
            }
        }
    }
}


@Composable
private fun DayHeader(date: LocalDate) {
    val label = remember(date) { formatDayHeader(date) }
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = HedwigTheme.colors.secondaryText,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun TimedText(
    text: String,
    modifier: Modifier = Modifier,
    meta: @Composable () -> Unit,
) {
    // Outlives recompositions on purpose: Text only re-reports its layout when the layout changes.
    val textLayout = remember { TextLayoutHolder() }

    Layout(
        modifier = modifier,
        content = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
                onTextLayout = { textLayout.result = it },
            )
            Box { meta() }
        },
    ) { measurables, constraints ->
        val gap = 8.dp.roundToPx()
        val textPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))
        val metaPlaceable = measurables[1].measure(Constraints()) // its natural size
        val textResult = checkNotNull(textLayout.result) { "Text didn't report its layout" }

        val lastLineEnd = textResult.getLineRight(textResult.lineCount - 1).roundToInt()
        val fitsOnLastLine = lastLineEnd + gap + metaPlaceable.width <= constraints.maxWidth

        val width: Int
        val height: Int
        if (fitsOnLastLine) {
            width = maxOf(textPlaceable.width, lastLineEnd + gap + metaPlaceable.width)
            height = textPlaceable.height
        } else {
            width = maxOf(textPlaceable.width, metaPlaceable.width)
            height = textPlaceable.height + metaPlaceable.height
        }

        layout(constraints.constrainWidth(width), constraints.constrainHeight(height)) {
            textPlaceable.placeRelative(0, 0)
            metaPlaceable.placeRelative(width - metaPlaceable.width, height - metaPlaceable.height)
        }
    }
}

@Composable
private fun MessageMeta(time: String, status: MessageStatus?) {
    val dimColor = LocalContentColor.current.copy(alpha = 0.65f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 14.sp, fontFeatureSettings = "tnum"),
            color = dimColor,
            maxLines = 1,
        )
        if (status != null) StatusIcon(status, dimColor)
    }
}

@Composable
private fun StatusIcon(status: MessageStatus, dimColor: Color) {
    val (icon, tint, description) = when (status) {
        MessageStatus.Pending -> Triple(Icons.Outlined.Schedule, dimColor, "Sending")
        MessageStatus.Sent -> Triple(Icons.Filled.Done, dimColor, "Sent")
        MessageStatus.Read -> Triple(Icons.Filled.DoneAll, HedwigTheme.colors.readTick, "Read")
    }
    Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(16.dp))
}

private class TextLayoutHolder {
    var result: TextLayoutResult? = null
}


private val BubbleCorner = 12.dp
private val BubbleTail = 4.dp

/** The first bubble of a group gets a small corner on the sender's side. */
private fun bubbleShape(isMine: Boolean, isFirstInGroup: Boolean): Shape = when {
    !isFirstInGroup -> RoundedCornerShape(BubbleCorner)
    isMine -> RoundedCornerShape(BubbleCorner, BubbleTail, BubbleCorner, BubbleCorner)
    else -> RoundedCornerShape(BubbleTail, BubbleCorner, BubbleCorner, BubbleCorner)
}

/** Positions a bubble: the group gap above it, its side, and the 56dp gutter on the far side. */
@Composable
private fun BubbleRow(isMine: Boolean, isFirstInGroup: Boolean, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // With the list's 2dp spacing: 2dp inside a group, 8dp between groups.
            .padding(top = if (isFirstInGroup) 6.dp else 0.dp)
            .padding(start = if (isMine) 56.dp else 0.dp, end = if (isMine) 0.dp else 56.dp),
        contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        content()
    }
}

private fun listPadding(scaffoldPadding: PaddingValues) = PaddingValues(
    start = 8.dp,
    end = 8.dp,
    top = scaffoldPadding.calculateTopPadding() + 8.dp,
    bottom = scaffoldPadding.calculateBottomPadding() + 8.dp,
)

@Composable
private fun MessageBubble(item: ChatEntry.MessageItem) {
    val colors = HedwigTheme.colors
    val timeFormat = rememberTimeFormat()
    val time = remember(item.message.timestamp, timeFormat) {
        item.message.timestamp?.let { timeFormat.format(it.toDate()) }.orEmpty()
    }

    BubbleRow(isMine = item.isMine, isFirstInGroup = item.isFirstInGroup) {
        Surface(
            color = if (item.isMine) colors.outgoingBubble else colors.incomingBubble,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = bubbleShape(item.isMine, item.isFirstInGroup),
            shadowElevation = 1.dp,
            // TalkBack reads "Yes! What time? 8:01 PM, Read" as one item, not two.
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            TimedText(
                text = item.message.text,
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp),
            ) {
                MessageMeta(time = time, status = item.status)
            }
        }
    }
}


@Composable
private fun MessageInput(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val colors = HedwigTheme.colors
    val textStyle = MaterialTheme.typography.bodyLarge.copy(
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface, // BasicTextField doesn't pick up LocalContentColor
    )

    Row(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom, // send stays next to the last line as the field grows
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = colors.incomingBubble, // white / #241F2A, the same as incoming bubbles
            shadowElevation = 1.dp,
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = textStyle,
                cursorBrush = SolidColor(colors.accent),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                decorationBox = { innerTextField ->
                    // Padding inside the decoration box, so taps on it still focus the field.
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 13.dp)) {
                        if (text.isEmpty()) {
                            Text("Message", style = textStyle, color = colors.secondaryText)
                        }
                        innerTextField()
                    }
                },
            )
        }
        FilledIconButton(
            onClick = onSendClick,
            enabled = text.isNotBlank(),
            modifier = Modifier.size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = colors.accent,
                contentColor = Color.White,
                disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                disabledContentColor = Color.White,
            ),
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun TopBarShimmer() {
    val boneColor = LocalContentColor.current // white on the purple bar, light grey on the dark one
    Row(verticalAlignment = Alignment.CenterVertically) {
        Shimmer(Modifier.size(40.dp), shape = CircleShape, color = boneColor)
        Spacer(Modifier.width(12.dp))
        Column {
            Shimmer(Modifier.size(width = 128.dp, height = 16.dp), shape = RoundedCornerShape(8.dp), color = boneColor)
            Spacer(Modifier.height(8.dp))
            Shimmer(Modifier.size(width = 88.dp, height = 12.dp), shape = RoundedCornerShape(6.dp), color = boneColor)
        }
    }
}


private data class ShimmerBubble(val isMine: Boolean, val isFirstInGroup: Boolean, val width: Dp, val twoLines: Boolean = false)


private val ShimmerBubbles = listOf(
    ShimmerBubble(isMine = false, isFirstInGroup = false, width = 220.dp, twoLines = true),
    ShimmerBubble(isMine = false, isFirstInGroup = true, width = 140.dp),
    ShimmerBubble(isMine = true, isFirstInGroup = true, width = 170.dp),
    ShimmerBubble(isMine = false, isFirstInGroup = true, width = 200.dp),
    ShimmerBubble(isMine = true, isFirstInGroup = false, width = 110.dp),
    ShimmerBubble(isMine = true, isFirstInGroup = true, width = 250.dp, twoLines = true),
    ShimmerBubble(isMine = false, isFirstInGroup = true, width = 120.dp),
    ShimmerBubble(isMine = true, isFirstInGroup = true, width = 150.dp),
    ShimmerBubble(isMine = false, isFirstInGroup = false, width = 236.dp, twoLines = true),
    ShimmerBubble(isMine = false, isFirstInGroup = true, width = 180.dp),
)

@Composable
private fun MessageListShimmer(contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = "Loading messages" },
        reverseLayout = true,
        userScrollEnabled = false,
        contentPadding = listPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        items(ShimmerBubbles) { bone ->
            BubbleRow(bone.isMine, bone.isFirstInGroup) {
                Shimmer(
                    modifier = Modifier.size(width = bone.width, height = if (bone.twoLines) 55.dp else 34.dp),
                    shape = bubbleShape(bone.isMine, bone.isFirstInGroup),
                )
            }
        }
    }
}

@Composable
private fun ErrorPill(message: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold),
            )
        }
    }
}


private val PreviewUser = User(uid = "user-2", name = "Riya Sharma")

private val PreviewEntries: List<ChatEntry> = listOf(
    ChatEntry.DayHeader(LocalDate.now()),
    ChatEntry.MessageItem(Message(id = "1", text = "Hey! Are we still on for tonight?"), isMine = false, isFirstInGroup = true, status = null),
    ChatEntry.MessageItem(Message(id = "2", text = "Yes! 8 pm works"), isMine = true, isFirstInGroup = true, status = MessageStatus.Read),
    ChatEntry.MessageItem(Message(id = "3", text = "I'll book a table at that new place near the station. Their pasta is supposed to be really good."), isMine = true, isFirstInGroup = false, status = MessageStatus.Sent),
    ChatEntry.MessageItem(Message(id = "4", text = "Perfect 🙌"), isMine = false, isFirstInGroup = true, status = null),
    ChatEntry.MessageItem(Message(id = "5", text = "Can we move it to 8:30?"), isMine = false, isFirstInGroup = false, status = null),
)

@Composable
private fun ChatPreview(uiState: ChatUIState, messageText: String = "") {
    HedwigTheme {
        ChatContent(
            uiState = uiState,
            messageText = messageText,
            onMessageTextChange = {},
            onSendClick = {},
            onBackClick = {},
            onWaveClick = {},
        )
    }
}

@PhoneLightPreview
@Composable
private fun ChatPreviewLight() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, entries = PreviewEntries),
    messageText = "See you there",
)

@PhoneDarkPreview
@Composable
private fun ChatPreviewDark() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, entries = PreviewEntries),
)

@PhoneLightPreview
@Composable
private fun ChatEmptyPreview() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser),
)

@PhoneLightPreview
@Composable
private fun ChatErrorPreview() = ChatPreview(
    ChatUIState(isLoading = false, otherUser = PreviewUser, entries = PreviewEntries, errorMessage = "Message not sent. Tap send to try again."),
    messageText = "Can we move it to 8:30?",
)

@PhoneDarkPreview
@Composable
private fun ChatLoadingPreview() = ChatPreview(ChatUIState())
