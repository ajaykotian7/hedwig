package dev.ajaykotian.hedwig.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.chats.ChatsTab
import dev.ajaykotian.hedwig.ui.chats.ChatsTabContent
import dev.ajaykotian.hedwig.ui.chats.ChatsUIState
import dev.ajaykotian.hedwig.ui.components.HedwigTopAppBarDefaults
import dev.ajaykotian.hedwig.ui.components.LightStatusBarIcons
import dev.ajaykotian.hedwig.ui.profile.ProfileTab
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import kotlinx.coroutines.launch

private enum class HomeTab(val title: String) {
    Chats("Chats"),
    Profile("Profile")
}

@Composable
fun HomeScreen(onNewChatClick: () -> Unit, onChatClick: (otherUserId: String) -> Unit) {
    LightStatusBarIcons()
    RequestNotificationPermission()
    HomeContent(
        onNewChatClick = onNewChatClick,
        chatsTab = { ChatsTab(onChatClick = onChatClick) },
        profileTab = { ProfileTab() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    onNewChatClick: () -> Unit,
    chatsTab: @Composable () -> Unit,
    profileTab: @Composable () -> Unit,
    initialTab: HomeTab = HomeTab.Chats,
) {
    val colors = HedwigTheme.colors
    val tabs = HomeTab.entries
    val pagerState = rememberPagerState(
        initialPage = tabs.indexOf(initialTab),
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp, lineHeight = 32.sp)
                        )
                    },
                    colors = HedwigTopAppBarDefaults.colors()
                )
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = colors.topBar,
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(pagerState.currentPage, matchContentSize = true),
                            width = Dp.Unspecified,
                            color = colors.selectedTab
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = pagerState.currentPage == index
                        Tab(
                            selected = selected,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                            selectedContentColor = colors.selectedTab,
                            unselectedContentColor = colors.onTopBar.copy(alpha = 0.7f),
                            text = {
                                Text(
                                    tab.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = tabs[pagerState.currentPage] == HomeTab.Chats,
                enter = fadeIn() + scaleIn(initialScale = 0.6f),
                exit = fadeOut() + scaleOut(targetScale = 0.6f),
            ) {
                FloatingActionButton(
                    onClick = onNewChatClick,
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.AddComment, contentDescription = "New chat")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            beyondViewportPageCount = 1
        ) { page ->
            when (tabs[page]) {
                HomeTab.Chats -> chatsTab()
                HomeTab.Profile -> profileTab()
            }
        }
    }
}


/** Asks for notification permission */
@Composable
private fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return // granted at install on older versions

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Granted or denied, nothing else to do: NotificationHelper checks before every notification.
    }
    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}


@Composable
private fun HomePreview(initialTab: HomeTab = HomeTab.Chats) {
    HedwigTheme {
        HomeContent(
            onNewChatClick = {},
            chatsTab = { ChatsTabContent(ChatsUIState(isLoading = false), onChatClick = {}) },
            profileTab = {},
            initialTab = initialTab,
        )
    }
}

@PhoneLightPreview
@Composable
private fun HomeChatsPreview() = HomePreview()

@PhoneDarkPreview
@Composable
private fun HomeChatsDarkPreview() = HomePreview()

@PhoneLightPreview
@Composable
private fun HomeProfilePreview() = HomePreview(initialTab = HomeTab.Profile)