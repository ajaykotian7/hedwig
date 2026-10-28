package dev.ajaykotian.hedwig.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.ajaykotian.hedwig.data.model.User
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.base.screenViewModel
import dev.ajaykotian.hedwig.ui.components.Avatar
import dev.ajaykotian.hedwig.ui.components.PrimaryButton
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
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .padding(24.dp),
        ) {
            StepHeader("Set up your profile", "Add your details so people know it's you")
            Spacer(Modifier.height(32.dp))
            Avatar(
                name = uiState.nameDraft,
                id = uiState.user.uid,
                size = 96.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                label = { Text("Your name") },
                value = uiState.nameDraft,
                singleLine = true,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
                enabled = !(uiState.isLoading || uiState.isSaving)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                label = { Text("About") },
                minLines = 5,
                value = uiState.aboutDraft,
                onValueChange = onAboutChange,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { if (uiState.canSave) onSave() }),
                enabled = !(uiState.isLoading || uiState.isSaving)
            )
            uiState.errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.weight(1f))
            PrimaryButton(
                text = "Next",
                onClick = onSave,
                enabled = uiState.canSave,
                loading = uiState.isSaving,
                loadingText = "Saving…"
            )
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

@PhoneLightPreview
@Composable
private fun ProfileNewUserPreview() = ProfileSetupPreview(ProfileUIState(isLoading = false, user = previewUser))

@PhoneLightPreview
@Composable
private fun ProfileTypingPreview() =
    ProfileSetupPreview(ProfileUIState(isLoading = false, user = previewUser, nameDraft = "Ajay Kotian"))

@PhoneLightPreview
@Composable
private fun ProfileSavingPreview() =
    ProfileSetupPreview(ProfileUIState(isLoading = false, user = previewUser, nameDraft = "Ajay Kotian", isSaving = true))

@PhoneDarkPreview
@Composable
private fun ProfileErrorDarkPreview() = ProfileSetupPreview(
    ProfileUIState(isLoading = false, user = previewUser, nameDraft = "Ajay Kotian", errorMessage = "Couldn't save your profile."),
)

@PhoneLightPreview
@Composable
private fun ProfileLoadingPreview() = ProfileSetupPreview(ProfileUIState())
