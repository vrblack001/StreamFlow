package dev.streamflow.android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.streamflow.android.R
import dev.streamflow.android.ui.navigation.Screen

/** Global bottom navigation bar */
@Composable
fun StreamFlowBottomBar(navController: NavController, selectedRoute: String) {
    val items = listOf(
        BottomNavItem(Screen.Home.route,          Icons.Default.Home,          R.string.nav_home),
        BottomNavItem(Screen.Trending.route,      Icons.Default.Whatshot,      R.string.nav_trending),
        BottomNavItem(Screen.Subscriptions.route, Icons.Default.Subscriptions, R.string.nav_subscriptions),
        BottomNavItem(Screen.Library.route,       Icons.Default.VideoLibrary,  R.string.nav_library)
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = stringResource(item.labelRes)) },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val labelRes: Int
)

/** Reusable loading indicator */
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/** Reusable error view with retry button */
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.retry))
        }
    }
}

/** Comment item */
@Composable
fun CommentItem(comment: dev.streamflow.android.data.model.Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        coil.compose.AsyncImage(
            model = comment.thumbnail,
            contentDescription = null,
            modifier = Modifier
                .size(36.dp)
                .androidx.compose.ui.draw.clip(androidx.compose.foundation.shape.CircleShape)
        )
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(comment.author, style = MaterialTheme.typography.labelMedium)
                if (comment.isPinned) {
                    Icon(Icons.Default.PushPin, contentDescription = "Pinned",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
            Text(comment.commentText, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(comment.commentedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(14.dp))
                Text("${comment.likeCount}",
                    style = MaterialTheme.typography.labelSmall)
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

/** Chapter item */
@Composable
fun ChapterItem(
    chapter: dev.streamflow.android.data.model.Chapter,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(chapter.title) },
        supportingContent = {
            Text(chapter.start.let { secs ->
                "%d:%02d".format(secs / 60, secs % 60)
            })
        },
        leadingContent = {
            coil.compose.AsyncImage(
                model = chapter.image,
                contentDescription = null,
                modifier = Modifier.size(80.dp, 45.dp)
            )
        },
        modifier = Modifier.fillMaxWidth()
            .androidx.compose.foundation.clickable(onClick = onClick)
    )
}

/** Search history section */
@Composable
fun SearchHistorySection(
    history: List<String>,
    onItemClick: (String) -> Unit,
    onClearHistory: () -> Unit
) {
    if (history.isEmpty()) return
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Recent Searches", style = MaterialTheme.typography.titleSmall)
            TextButton(onClick = onClearHistory) { Text("Clear") }
        }
        history.forEach { query ->
            ListItem(
                headlineContent = { Text(query) },
                leadingContent = {
                    Icon(Icons.Default.History, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth()
                    .androidx.compose.foundation.clickable { onItemClick(query) }
            )
        }
    }
}
