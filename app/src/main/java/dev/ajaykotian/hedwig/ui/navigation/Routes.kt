package dev.ajaykotian.hedwig.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object LoginRoute

@Serializable
data object ProfileSetupRoute

@Serializable
data object HomeRoute

@Serializable
data object NewChatRoute

@Serializable
data class ChatRoute(val otherUserId: String)
