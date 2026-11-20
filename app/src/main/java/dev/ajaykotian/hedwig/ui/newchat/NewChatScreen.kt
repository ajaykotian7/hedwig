package dev.ajaykotian.hedwig.ui.newchat

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.HedwigTopAppBarDefaults
import dev.ajaykotian.hedwig.ui.components.StatusBarIcons
import dev.ajaykotian.hedwig.ui.components.Shimmer
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactClick: (uid: String) -> Unit,
    viewModel: NewChatViewModel = screenViewModel { getNewChatViewModel() },
) {
    StatusBarIcons(onColoredTopBar = true)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    NewChatContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onContactClick = onContactClick,
        onQueryChange = viewModel::onQueryChange,
        query = viewModel.query,
        onInviteClick = { context.shareInvite() },
    )
}

/** Opens Android's share sheet with an invite message. */
private fun Context.shareInvite() {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Let's chat on hedwig! https://github.com/ajaykotian7/hedwig")
    }
    startActivity(Intent.createChooser(send, "Invite a friend"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatContent(
    uiState: NewChatUIState,
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    onInviteClick: () -> Unit
) {

    var isSearching by rememberSaveable { mutableStateOf(false) }
    val closeSearch = {
        isSearching = false
        onQueryChange("")
    }
    // System back closes search before it leaves the screen.
    BackHandler(enabled = isSearching, onBack = closeSearch)

    val colors = HedwigTheme.colors
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = if (isSearching) closeSearch else onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = if (isSearching) "Close search" else "Back")
                    }
                },
                title = {
                    if (isSearching) {
                        SearchField(query = query, onQueryChange = onQueryChange)
                    } else {
                        Column {
                            Text(
                                text = "Select contact",
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 20.sp, lineHeight = 26.sp),
                            )
                            if (!uiState.isLoading) {
                                Text(
                                    text = pluralStringResource(R.plurals.contact_count, uiState.users.size, uiState.users.size),
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontFeatureSettings = "tnum"),
                                    color = LocalContentColor.current.copy(alpha = 0.75f),
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (!isSearching) {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                    } else if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                colors = HedwigTopAppBarDefaults.colors()
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val fill = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            uiState.isLoading -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding() + 8.dp)
                    // TalkBack reads one "Loading contacts" instead of eight unlabeled shapes.
                    .clearAndSetSemantics { contentDescription = "Loading contacts" },
            ) {
                SkeletonWidths.forEach { (name, about) ->
                    ShimmerContactRow(nameWidth = name, aboutWidth = about)
                }
            }

            uiState.errorMessage != null -> EmptyState(
                icon = Icons.Outlined.ErrorOutline,
                title = "Couldn't load contacts",
                message = uiState.errorMessage,
                modifier = fill,
            )

            uiState.users.isEmpty() && uiState.query.isNotBlank() -> EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = "No results",
                message = "Nobody matches \"${uiState.query.trim()}\".",
                modifier = fill,
            )

            uiState.users.isEmpty() -> EmptyState(
                icon = Icons.Outlined.PersonAdd,
                title = "No one here yet",
                message = "Invite a friend to hedwig and they will show up here.",
                modifier = fill,
                action = {
                    Button(
                        onClick = onInviteClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HedwigTheme.colors.accent,
                            contentColor = Color.White,
                        ),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                    ) {
                        Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Invite a friend", fontWeight = FontWeight.Bold)
                    }
                },
            )

            else -> {
                // Recomputed only when the list changes, not on every recomposition.
                val sections = remember(uiState.users) { uiState.users.groupBy { it.sectionLetter() } }

                LazyColumn(
                    // Top as padding, so sticky headers pin below the bar instead of behind it.
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding()),
                ) {

                    if (uiState.query.isNotBlank()) {
                        item(key = "results-label", contentType = "label") {
                            Text(
                                text = pluralStringResource(R.plurals.result_count, uiState.users.size, uiState.users.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = HedwigTheme.colors.secondaryText,
                                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
                            )
                        }
                        items(uiState.users, key = { it.uid }, contentType = { "contact" }) { user ->
                            ContactRow(user = user, highlight = uiState.query.trim(), onClick = { onContactClick(user.uid) })
                        }
                    } else {
                        sections.forEach { (letter, users) ->
                            stickyHeader(key = "header-$letter", contentType = "header") {
                                SectionHeader(letter)
                            }
                            items(users, key = { it.uid }, contentType = { "contact" }) { user ->
                                ContactRow(user = user, onClick = { onContactClick(user.uid) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = HedwigTheme.colors
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        placeholder = { Text("Search name or number") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = colors.onTopBar,
            unfocusedTextColor = colors.onTopBar,
            cursorColor = colors.onTopBar,
            focusedPlaceholderColor = colors.onTopBar.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = colors.onTopBar.copy(alpha = 0.7f),
        ),
    )
}


@Composable
private fun ContactRow(user: User, onClick: () -> Unit, highlight: String = "") {
    val accent = HedwigTheme.colors.accentText
    val name = remember(user.name, highlight, accent) { highlighted(user.name, highlight, accent) }
    ListItem(
        headlineContent = {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = { Text(user.about, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Avatar(name = user.name, id = user.uid) },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            supportingColor = HedwigTheme.colors.secondaryText,
        ),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** [text] with every case-insensitive occurrence of [query] drawn in [color] ExtraBold. */
private fun highlighted(text: String, query: String, color: Color): AnnotatedString = buildAnnotatedString {
    append(text)
    if (query.isEmpty()) return@buildAnnotatedString
    var start = text.indexOf(query, ignoreCase = true)
    while (start >= 0) {
        addStyle(SpanStyle(color = color, fontWeight = FontWeight.ExtraBold), start, start + query.length)
        start = text.indexOf(query, startIndex = start + query.length, ignoreCase = true)
    }
}


/** Varied bar widths, so the skeleton reads like a list of real names. */
private val SkeletonWidths = listOf(
    130.dp to 90.dp, 110.dp to 150.dp, 150.dp to 70.dp, 120.dp to 180.dp,
    140.dp to 100.dp, 100.dp to 130.dp, 130.dp to 160.dp, 110.dp to 90.dp,
)

/** A ContactRow stand-in: same ListItem and avatar size, so rows don't shift when data arrives. */
@Composable
private fun ShimmerContactRow(nameWidth: Dp, aboutWidth: Dp) {
    ListItem(
        headlineContent = { Shimmer(Modifier.size(width = nameWidth, height = 14.dp)) },
        supportingContent = {
            Shimmer(
                Modifier
                    .padding(top = 8.dp)
                    .size(width = aboutWidth, height = 12.dp)
            )
        },
        leadingContent = { Shimmer(Modifier.size(48.dp), shape = CircleShape) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}


@Composable
private fun SectionHeader(letter: Char) {
    Text(
        text = letter.toString(),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.ExtraBold,
        color = HedwigTheme.colors.accentText,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
    )
}

/** "Priya" -> 'P'. Names starting with a digit or emoji go under '#'. */
private fun User.sectionLetter(): Char =
    name.firstOrNull()?.uppercaseChar()?.takeIf { it.isLetter() } ?: '#'


private val previewUsers = listOf(
    User(uid = "u1", name = "Aarav Patel", about = "Available", phoneNumber = "+919800000001"),
    User(uid = "u2", name = "Ananya Rao", about = "At the gym", phoneNumber = "+919800000002"),
    User(uid = "u3", name = "Kabir Singh", about = "Busy", phoneNumber = "+919800000003"),
    User(uid = "u4", name = "Meera Nair", about = User.DEFAULT_ABOUT, phoneNumber = "+919800000004"),
    User(uid = "u5", name = "Priya Sharma", about = "At work", phoneNumber = "+919800000005"),
    User(uid = "u6", name = "Rahul Mehta", about = "Battery about to die", phoneNumber = "+919800000006"),
    User(uid = "u7", name = "Sneha Iyer", about = "In a meeting", phoneNumber = "+919800000007"),
    User(uid = "u8", name = "Vikram Joshi", about = "Sleeping", phoneNumber = "+919800000008"),
)

@Composable
private fun NewChatPreview(state: NewChatUIState, query: String = "") {
    HedwigTheme {
        NewChatContent(
            uiState = state,
            query = query,
            onQueryChange = {},
            onBackClick = {},
            onContactClick = {},
            onInviteClick = {},
        )
    }
}

@PhoneLightPreview
@Composable
private fun NewChatSearchPreview() {
    val query = "ra"
    NewChatPreview(
        NewChatUIState(
            isLoading = false,
            query = query,
            users = previewUsers.filter { it.name.contains(query, ignoreCase = true) },
        ),
        query = query,
    )
}


@PhoneLightPreview
@Composable
private fun NewChatListPreview() = NewChatPreview(NewChatUIState(isLoading = false, users = previewUsers))

@PhoneDarkPreview
@Composable
private fun NewChatListDarkPreview() = NewChatPreview(NewChatUIState(isLoading = false, users = previewUsers))

@PhoneLightPreview
@Composable
private fun NewChatEmptyPreview() = NewChatPreview(NewChatUIState(isLoading = false))

@PhoneLightPreview
@Composable
private fun NewChatNoResultsPreview() =
    NewChatPreview(NewChatUIState(isLoading = false, query = "zz"), query = "zz")

@PhoneDarkPreview
@Composable
private fun NewChatLoadingDarkPreview() = NewChatPreview(NewChatUIState())

