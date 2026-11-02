package dev.ajaykotian.hedwig.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.formatPhoneNumber
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.ErrorRow
import dev.ajaykotian.hedwig.ui.components.HedwigTextFieldDefaults
import dev.ajaykotian.hedwig.ui.components.Shimmer
import dev.ajaykotian.hedwig.ui.components.SupportingRow
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import kotlinx.coroutines.flow.first

private enum class ProfileDialog { EditName, EditAbout, LogOut }

@Composable
fun ProfileTab(viewModel: ProfileViewModel = screenViewModel { getProfileViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileTabContent(
        uiState = uiState,
        onSaveName = viewModel::updateName,
        onSaveAbout = viewModel::updateAbout,
        onLogOut = viewModel::signOut
    )
}

@Composable
private fun ProfileTabContent(
    uiState: ProfileUIState,
    onSaveName: (String) -> Unit,
    onSaveAbout: (String) -> Unit,
    onLogOut: () -> Unit,
) {
    var dialog by rememberSaveable { mutableStateOf<ProfileDialog?>(null) }
    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {

        if (uiState.isLoading) {
            Shimmer(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 32.dp, bottom = 28.dp)
                    .size(112.dp),
                shape = CircleShape,
            )
            ProfileCard {
                ShimmerRow(valueWidth = 140.dp)
                RowDivider()
                ShimmerRow(valueWidth = 100.dp)
                RowDivider()
                ShimmerRow(valueWidth = 150.dp)
            }
        } else {
            Avatar(
                name = user.name,
                id = user.uid,
                size = 112.dp,
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 28.dp)
                    .align(Alignment.CenterHorizontally),
            )

            ProfileCard {
                ProfileRow(Icons.Outlined.Person, label = "Name", value = user.name, onClick = { dialog = ProfileDialog.EditName })
                RowDivider()
                ProfileRow(Icons.Outlined.Info, label = "About", value = user.about, onClick = { dialog = ProfileDialog.EditAbout })
                RowDivider()
                ProfileRow(Icons.Outlined.Phone, label = "Phone", value = formatPhoneNumber(user.phoneNumber))
            }
        }

        Text(
            text = "Your name and about are visible to people you chat with.",
            style = MaterialTheme.typography.bodySmall,
            color = HedwigTheme.colors.secondaryText,
            modifier = Modifier.padding(start = 4.dp, top = 12.dp, end = 4.dp),
        )

        uiState.errorMessage?.let {
            ErrorRow(it, Modifier.padding(top = 12.dp))
        }

        Spacer(Modifier.height(24.dp))
        ProfileCard {
            LogOutRow(onClick = { dialog = ProfileDialog.LogOut })
        }
        Spacer(Modifier.height(24.dp))
    }
    when (dialog) {
        ProfileDialog.EditName -> EditTextDialog(
            title = "Your name",
            initialValue = user.name,
            hint = "Shown to people you chat with",
            maxLength = ProfileUIState.MAX_NAME_LENGTH,
            singleLine = true,
            capitalization = KeyboardCapitalization.Words,
            onDismiss = { dialog = null },
            onSave = {
                onSaveName(it)
                dialog = null
            },
        )

        ProfileDialog.EditAbout -> EditTextDialog(
            title = "About",
            initialValue = user.about,
            hint = null,
            maxLength = ProfileUIState.MAX_ABOUT_LENGTH,
            singleLine = false,
            capitalization = KeyboardCapitalization.Sentences,
            onDismiss = { dialog = null },
            onSave = {
                onSaveAbout(it)
                dialog = null
            },
        )

        ProfileDialog.LogOut -> LogOutDialog(
            onConfirm = {
                dialog = null
                onLogOut()
            },
            onDismiss = { dialog = null },
        )

        null -> {}
    }
}

@Composable
private fun ShimmerRow(valueWidth: Dp) {
    ListItem(
        overlineContent = { Shimmer(Modifier.size(width = 56.dp, height = 10.dp)) },
        headlineContent = {
            Shimmer(
                Modifier
                    .padding(top = 6.dp)
                    .size(width = valueWidth, height = 14.dp)
            )
        },
        leadingContent = { Shimmer(Modifier.size(40.dp), shape = CircleShape) },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

/** Groups rows on a rounded panel, like the profile setup preview card. */
@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HedwigTheme.colors.panel,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(content = content)
    }
}

/** A label/value row. With [onClick] it becomes tappable and shows an edit icon. */
@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    val colors = HedwigTheme.colors
    ListItem(
        overlineContent = { Text(label, fontWeight = FontWeight.SemiBold) },
        headlineContent = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = { IconBadge(icon, background = colors.selectedRow, tint = colors.accentText) },
        trailingContent = if (onClick != null) {
            { Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(20.dp)) }
        } else {
            null
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            overlineColor = colors.secondaryText,
            trailingIconColor = colors.secondaryText,
        ),
        modifier = if (onClick != null) {
            Modifier.clickable(onClickLabel = "Edit $label", onClick = onClick)
        } else {
            Modifier.semantics(mergeDescendants = true) {}
        },
    )
}

@Composable
private fun LogOutRow(onClick: () -> Unit) {
    val error = MaterialTheme.colorScheme.error
    ListItem(
        headlineContent = { Text("Log out", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold) },
        leadingContent = {
            IconBadge(Icons.AutoMirrored.Filled.Logout, background = error.copy(alpha = 0.12f), tint = error)
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent, headlineColor = error),
        modifier = Modifier.clickable(onClick = onClick),
    )
}

/** A 20dp icon in a 40dp tinted circle. */
@Composable
private fun IconBadge(icon: ImageVector, background: Color, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(background, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** Inset to line up with the row text: 16 padding + 40 badge + 16 gap. */
@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(start = 72.dp), color = HedwigTheme.colors.divider)
}


@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    hint: String?,
    maxLength: Int,
    singleLine: Boolean,
    capitalization: KeyboardCapitalization,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    // TextFieldValue (not String) so the cursor can start at the end of the current text.
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initialValue, selection = TextRange(initialValue.length)))
    }
    val canSave = value.text.isNotBlank()

    val focusRequester = remember { FocusRequester() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            val windowInfo = LocalWindowInfo.current
            LaunchedEffect(windowInfo) {
                // A dialog is its own window; focus only sticks once that window has focus.
                snapshotFlow { windowInfo.isWindowFocused }.first { it }
                focusRequester.requestFocus()
            }

            OutlinedTextField(
                value = value,
                onValueChange = { new ->
                    value = new.copy(text = new.text.replace('\n', ' ').take(maxLength))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else 4,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                shape = HedwigTextFieldDefaults.Shape,
                colors = HedwigTextFieldDefaults.colors(),
                supportingText = { SupportingRow(hint = hint, count = value.text.length, max = maxLength) },
                keyboardOptions = KeyboardOptions(capitalization = capitalization, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canSave) onSave(value.text) }),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.text) }, enabled = canSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun LogOutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log out?") },
        text = { Text("You will need to verify your phone number again to sign back in.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("Log out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private val previewState = ProfileUIState(
    isLoading = false,
    user = User(uid = "user-1", name = "Ajay Kotian", about = "At work", phoneNumber = "+919876543210"),
)

@Composable
private fun ProfileTabPreview(state: ProfileUIState) {
    HedwigTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileTabContent(uiState = state, onSaveName = {}, onSaveAbout = {}, onLogOut = {})
        }
    }
}

// Profile Loaded
@PhoneLightPreview
@Composable
private fun ProfileTabLightPreview() = ProfileTabPreview(previewState)

// Loading State
@PhoneLightPreview
@Composable
private fun ProfileTabLoadingPreview() = ProfileTabPreview(ProfileUIState())

// Dark : Profile Loaded
@PhoneDarkPreview
@Composable
private fun ProfileTabDarkPreview() = ProfileTabPreview(previewState)

// Save Failure
@PhoneLightPreview
@Composable
private fun ProfileTabErrorPreview() = ProfileTabPreview(
    previewState.copy(errorMessage = "Couldn't save your profile. Check your connection and try again."),
)


