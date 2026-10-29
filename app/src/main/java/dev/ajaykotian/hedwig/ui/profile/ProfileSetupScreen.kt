package dev.ajaykotian.hedwig.ui.profile

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.ComponentPreviews
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.ErrorRow
import dev.ajaykotian.hedwig.ui.components.FieldLabel
import dev.ajaykotian.hedwig.ui.components.HedwigTextFieldDefaults
import dev.ajaykotian.hedwig.ui.components.PrimaryButton
import dev.ajaykotian.hedwig.ui.components.Shimmer
import dev.ajaykotian.hedwig.ui.login.StepHeader
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme


@Composable
fun ProfileSetupScreen(onProfileSaved: () -> Unit, viewModel: ProfileViewModel = screenViewModel { getProfileViewModel() }) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onProfileSaved()
    }

    ProfileSetupContent(
        uiState,
        onNameChange = viewModel::onNameDraftChanged,
        onAboutChange = viewModel::onAboutDraftChanged,
        onSave = viewModel::saveProfile,
    )
}

@Composable
private fun ProfileSetupContent(
    uiState: ProfileUIState,
    onNameChange: (String) -> Unit,
    onAboutChange: (String) -> Unit,
    onSave: () -> Unit,
) {

    val fieldsEnabled = !uiState.isLoading && !uiState.isSaving
    val nameFocus = remember { FocusRequester() }

    // A new user lands with the keyboard already open on the name field.
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.nameDraft.isEmpty()) nameFocus.requestFocus()
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                StepHeader("Set up your profile", "Add your details so people know it's you")
                Spacer(Modifier.height(28.dp))
                FieldLabel("How you'll appear in chats")
                Spacer(Modifier.height(8.dp))
                ProfilePreviewCard(
                    name = uiState.nameDraft,
                    about = uiState.aboutDraft,
                    uid = uiState.user.uid,
                    isLoading = uiState.isLoading
                )
                Spacer(Modifier.height(24.dp))
                FieldLabel("Name", enabled = fieldsEnabled)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.nameDraft,
                    placeholder = {
                        if (uiState.isLoading) Shimmer(
                            Modifier
                                .width(120.dp)
                                .height(14.dp)
                        )
                        else Text("Your full name")
                    },
                    singleLine = true,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                    enabled = fieldsEnabled,
                    shape = HedwigTextFieldDefaults.Shape,
                    colors = HedwigTextFieldDefaults.colors(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    supportingText = {
                        SupportingRow(
                            hint = "Shown to people you chat with",
                            count = if (uiState.isLoading) null else uiState.nameDraft.length,
                            max = ProfileUIState.MAX_NAME_LENGTH,
                        )
                    },
                )
                Spacer(Modifier.height(8.dp))
                FieldLabel("About", enabled = fieldsEnabled)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    minLines = 2,
                    maxLines = 4,
                    value = if (uiState.isLoading) "" else uiState.aboutDraft,
                    placeholder = if (uiState.isLoading) {
                        {
                            Shimmer(
                                Modifier
                                    .width(210.dp)
                                    .height(14.dp)
                            )
                        }
                    } else {
                        null
                    },
                    onValueChange = onAboutChange,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { if (uiState.canSave) onSave() }),
                    enabled = fieldsEnabled,
                    shape = HedwigTextFieldDefaults.Shape,
                    colors = HedwigTextFieldDefaults.colors(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    supportingText = {
                        SupportingRow(
                            hint = null,
                            count = if (uiState.isLoading) null else uiState.aboutDraft.length,
                            max = ProfileUIState.MAX_ABOUT_LENGTH
                        )
                    },
                )
                if (!uiState.isLoading) {
                    Spacer(Modifier.height(8.dp))
                    AboutPresetChips(uiState.aboutDraft, fieldsEnabled, onSelect = onAboutChange)
                }
                uiState.errorMessage?.let { ErrorRow(it, Modifier.padding(top = 16.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "Continue",
                onClick = onSave,
                enabled = uiState.canSave,
                loading = uiState.isSaving,
                loadingText = "Saving…"
            )
            Text(
                text = "You can change these anytime.",
                style = MaterialTheme.typography.bodySmall,
                color = HedwigTheme.colors.secondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun ProfilePreviewCard(name: String, about: String, uid: String, isLoading: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = HedwigTheme.colors.panel,
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                Shimmer(Modifier.size(56.dp), shape = CircleShape)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Shimmer(
                        Modifier
                            .width(140.dp)
                            .height(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Shimmer(
                        Modifier
                            .width(200.dp)
                            .height(12.dp)
                    )
                }
            } else {
                Avatar(
                    name = name,
                    id = uid,
                    size = 56.dp,
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = name.trim().ifEmpty { "Your name" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (name.isBlank()) FontWeight.SemiBold else FontWeight.Bold,
                        color = if (name.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = about.trim().ifEmpty { User.DEFAULT_ABOUT },
                        style = MaterialTheme.typography.bodyMedium,
                        color = HedwigTheme.colors.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private val AboutPresets = listOf(
    User.DEFAULT_ABOUT,
    "Available",
    "Busy",
    "At work",
    "In a meeting",
    "Sleeping",
    "Battery about to die",
)

@Composable
private fun AboutPresetChips(
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AboutPresets.forEach { value ->
            val isSelected = value == selected.trim()
            FilterChip(
                selected = isSelected,
                enabled = enabled,
                label = { Text(value) },
                onClick = { onSelect(value) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else {
                    null
                },
                shape = CircleShape,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HedwigTheme.colors.selectedRow,
                    selectedLabelColor = HedwigTheme.colors.accentText,
                    selectedLeadingIconColor = HedwigTheme.colors.accentText,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun SupportingRow(hint: String?, count: Int?, max: Int) {
    Row(Modifier.fillMaxWidth()) {
        Text(hint.orEmpty(), modifier = Modifier.weight(1f))
        if (count != null) {
            Text("$count/$max", style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"))
        }
    }
}

@ComponentPreviews
@Composable
private fun ProfilePreviewCardPreview() {
    HedwigTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfilePreviewCard(name = "", about = User.DEFAULT_ABOUT, uid = "user-1", isLoading = false)
            ProfilePreviewCard(name = "Ajay Kotian", about = "At work", uid = "user-1", isLoading = false)
            ProfilePreviewCard(name = "A very long name that won't fit", about = "A long about line that also gets cut off at the end", uid = "user-2", isLoading = false)
            ProfilePreviewCard(name = "", about = "", uid = "user-1", isLoading = true)
        }
    }
}

@Composable
private fun ProfileSetupPreview(state: ProfileUIState) {
    HedwigTheme {
        ProfileSetupContent(uiState = state, onNameChange = {}, onAboutChange = {}, onSave = {})
    }
}

private val previewUser = User(uid = "user-1", phoneNumber = "+919876543210")


private val loadedState = ProfileUIState(isLoading = false, user = previewUser)

// New user, nothing typed yet.
@PhoneLightPreview
@Composable
private fun ProfileNewUserPreview() = ProfileSetupPreview(loadedState)

// Name typed, "At work" preset picked.
@PhoneLightPreview
@Composable
private fun ProfileTypingPreview() = ProfileSetupPreview(
    loadedState.copy(nameDraft = "Ajay Kotian", aboutDraft = "At work"),
)

// Saving
@PhoneLightPreview
@Composable
private fun ProfileSavingPreview() = ProfileSetupPreview(
    loadedState.copy(nameDraft = "Ajay Kotian", aboutDraft = "At work", isSaving = true),
)

// Save failed. With long message
@PhoneDarkPreview
@Composable
private fun ProfileErrorDarkPreview() = ProfileSetupPreview(
    loadedState.copy(
        nameDraft = "Ajay Kotian",
        errorMessage = "Couldn't save your profile. Check your connection and try again.",
    ),
)

// First Load
@PhoneDarkPreview
@Composable
private fun ProfileLoadingDarkPreview() = ProfileSetupPreview(ProfileUIState())

// Existing profile
@PhoneLightPreview
@Composable
private fun ProfileReturningUserPreview() {
    val saved = previewUser.copy(name = "Ajay Kotian", about = "Busy")
    ProfileSetupPreview(loadedState.copy(user = saved, nameDraft = saved.name, aboutDraft = saved.about))
}
