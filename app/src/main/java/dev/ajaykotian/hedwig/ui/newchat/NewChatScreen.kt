package dev.ajaykotian.hedwig.ui.newchat

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.LightStatusBarIcons
import dev.ajaykotian.hedwig.ui.components.LoadingScreen
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

@Composable
fun NewChatScreen(
    onBackClick: () -> Unit,
    onContactClick: (uid: String) -> Unit,
    viewModel: NewChatViewModel = screenViewModel { getNewChatViewModel() },
) {
    LightStatusBarIcons()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NewChatContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onContactClick = onContactClick,
        onQueryChange = viewModel::onQueryChange,
        query = viewModel.query,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatContent(
    uiState: NewChatUIState,
    onBackClick: () -> Unit,
    onContactClick: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit
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
                            Text("Select contact")
                            if (!uiState.isLoading) {
                                Text(
                                    text = pluralStringResource(R.plurals.contact_count, uiState.users.size, uiState.users.size),
                                    style = MaterialTheme.typography.bodySmall,
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.topBar,
                    titleContentColor = colors.onTopBar,
                    navigationIconContentColor = colors.onTopBar,
                    actionIconContentColor = colors.onTopBar,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val fill = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when {
            uiState.isLoading -> LoadingScreen(fill)

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
                icon = Icons.Outlined.PersonSearch,
                title = "No one here yet",
                message = "When friends sign up for hedwig, they'll show up here.",
                modifier = fill,
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPadding,
            ) {
                items(uiState.users, key = { it.uid }) { user ->
                    ContactRow(user = user, onClick = { onContactClick(user.uid) })
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
private fun ContactRow(user: User, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(user.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(user.about, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingContent = { Avatar(name = user.name, id = user.uid) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}


private val previewUsers = listOf(
    User(uid = "u1", name = "Priya Sharma", about = "At work", phoneNumber = "+919812345678"),
    User(uid = "u2", name = "Rahul Mehta", about = "Battery about to die", phoneNumber = "+919898765432"),
    User(uid = "u3", name = "Sneha Iyer", about = User.DEFAULT_ABOUT, phoneNumber = "+919811122233"),
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
        )
    }
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
