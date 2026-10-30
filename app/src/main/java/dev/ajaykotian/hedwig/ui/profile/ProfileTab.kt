package dev.ajaykotian.hedwig.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.ErrorRow
import dev.ajaykotian.hedwig.ui.components.LoadingScreen
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme

private enum class EditField { Name, About }

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
    var editing by rememberSaveable { mutableStateOf<EditField?>(null) }
    if (uiState.isLoading) {
        LoadingScreen()
        return
    }
    val user = uiState.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Avatar(
            name = user.name,
            id = user.uid,
            size = 120.dp,
            modifier = Modifier
                .padding(vertical = 32.dp)
                .align(Alignment.CenterHorizontally),
        )

        ProfileRow(Icons.Outlined.Person, label = "Name", value = user.name, onClick = { editing = EditField.Name })
        HorizontalDivider(Modifier.padding(start = 56.dp))
        ProfileRow(Icons.Outlined.Info, label = "About", value = user.about, onClick = { editing = EditField.About })
        HorizontalDivider(Modifier.padding(start = 56.dp))
        ProfileRow(Icons.Outlined.Phone, label = "Phone", value = user.phoneNumber)

        uiState.errorMessage?.let {
            ErrorRow(it, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        Spacer(Modifier.height(24.dp))
        ListItem(
            headlineContent = { Text("Log out") },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
            colors = ListItemDefaults.colors(
                headlineColor = MaterialTheme.colorScheme.error,
                leadingIconColor = MaterialTheme.colorScheme.error,
            ),
            modifier = Modifier.clickable(onClick = onLogOut),
        )
    }
    when (editing) {
        EditField.Name -> EditTextDialog(
            title = "Your name",
            initialValue = user.name,
            maxLength = ProfileUIState.MAX_NAME_LENGTH,
            singleLine = true,
            capitalization = KeyboardCapitalization.Words,
            onDismiss = { editing = null },
            onSave = {
                onSaveName(it)
                editing = null
            },
        )

        EditField.About -> EditTextDialog(
            title = "About",
            initialValue = user.about,
            maxLength = ProfileUIState.MAX_ABOUT_LENGTH,
            singleLine = false,
            capitalization = KeyboardCapitalization.Sentences,
            onDismiss = { editing = null },
            onSave = {
                onSaveAbout(it)
                editing = null
            },
        )

        null -> Unit
    }
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
) {
    ListItem(
        overlineContent = { Text(label) },
        headlineContent = { Text(value) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = if (onClick != null) {
            { Icon(Icons.Outlined.Edit, contentDescription = null) }
        } else {
            null
        },
        modifier = if (onClick != null) {
            Modifier.clickable(onClickLabel = "Edit $label", onClick = onClick)
        } else {
            Modifier
        },
    )
}

@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    maxLength: Int,
    singleLine: Boolean,
    capitalization: KeyboardCapitalization,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var text by rememberSaveable { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.replace('\n', ' ').take(maxLength) },
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else 4,
                supportingText = { Text("${text.length}/$maxLength") },
                keyboardOptions = KeyboardOptions(capitalization = capitalization),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }, enabled = text.isNotBlank()) { Text("Save") }
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

@PhoneLightPreview
@Composable
private fun ProfileTabLightPreview() = ProfileTabPreview(previewState)

@PhoneDarkPreview
@Composable
private fun ProfileTabDarkPreview() = ProfileTabPreview(previewState)

@PhoneLightPreview
@Composable
private fun ProfileTabErrorPreview() = ProfileTabPreview(
    previewState.copy(errorMessage = "Couldn't save your profile. Check your connection and try again."),
)


