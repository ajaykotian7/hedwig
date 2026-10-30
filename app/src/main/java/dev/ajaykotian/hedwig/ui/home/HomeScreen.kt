package dev.ajaykotian.hedwig.ui.home

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
import androidx.compose.material.icons.outlined.Forum
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import dev.ajaykotian.hedwig.R
import dev.ajaykotian.hedwig.ui.base.PhoneDarkPreview
import dev.ajaykotian.hedwig.ui.base.PhoneLightPreview
import dev.ajaykotian.hedwig.ui.components.EmptyState
import dev.ajaykotian.hedwig.ui.components.LightStatusBarIcons
import dev.ajaykotian.hedwig.ui.profile.ProfileTab
import dev.ajaykotian.hedwig.ui.theme.HedwigTheme
import kotlinx.coroutines.launch

private enum class HomeTab(val title: String) {
    Chats("Chats"),
    Profile("Profile")
}

@Composable
fun HomeScreen(onNewChatClick: () -> Unit) {
    LightStatusBarIcons()
    HomeContent(
        onNewChatClick = onNewChatClick,
        profileTab = { ProfileTab() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    onNewChatClick: () -> Unit,
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.topBar,
                        titleContentColor = colors.onTopBar,
                        actionIconContentColor = colors.onTopBar
                    )
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
                HomeTab.Chats -> EmptyState(
                    icon = Icons.Outlined.Forum,
                    title = "No chats yet",
                    message = "Tap the new chat button to message anyone on hedwig.",
                    modifier = Modifier.fillMaxSize(),
                )

                HomeTab.Profile -> profileTab()
            }
        }
    }
}


@Composable
private fun HomePreview(initialTab: HomeTab = HomeTab.Chats) {
    HedwigTheme {
        HomeContent(
            onNewChatClick = {},
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