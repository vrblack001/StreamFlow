package dev.streamflow.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import dev.streamflow.android.R
import dev.streamflow.android.ui.components.*
import dev.streamflow.android.ui.navigation.Screen
import dev.streamflow.android.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.prefs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings)) })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Network ──────────────────────────────────────────────────
            item {
                Text(stringResource(R.string.settings_network),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                var url by remember { mutableStateOf(prefs.pipedUrl) }
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(R.string.settings_piped_instance)) },
                    placeholder = { Text(stringResource(R.string.settings_piped_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        TextButton(onClick = { viewModel.savePipedUrl(url) }) {
                            Text(stringResource(R.string.save))
                        }
                    }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_disable_proxy),
                    subtitle = stringResource(R.string.settings_disable_proxy_desc),
                    checked = prefs.disableProxy,
                    onCheckedChange = { viewModel.setDisableProxy(it) }
                )
            }
            item { HorizontalDivider() }

            // ── Enhancements ─────────────────────────────────────────────
            item {
                Text(stringResource(R.string.settings_enhancements),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_sponsorblock),
                    subtitle = stringResource(R.string.settings_sponsorblock_desc),
                    checked = prefs.sponsorBlock,
                    onCheckedChange = { viewModel.setSponsorBlock(it) }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_ryd),
                    subtitle = stringResource(R.string.settings_ryd_desc),
                    checked = prefs.ryd,
                    onCheckedChange = { viewModel.setRyd(it) }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_dearrow),
                    subtitle = stringResource(R.string.settings_dearrow_desc),
                    checked = prefs.deArrow,
                    onCheckedChange = { viewModel.setDeArrow(it) }
                )
            }
            item { HorizontalDivider() }

            // ── Playback ─────────────────────────────────────────────────
            item {
                Text(stringResource(R.string.settings_playback),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_autoplay),
                    subtitle = stringResource(R.string.settings_autoplay_desc),
                    checked = prefs.autoPlay,
                    onCheckedChange = { viewModel.setAutoPlay(it) }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_background),
                    checked = prefs.backgroundPlay,
                    onCheckedChange = { viewModel.setBackgroundPlay(it) }
                )
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_pip),
                    checked = prefs.pip,
                    onCheckedChange = { viewModel.setPip(it) }
                )
            }
            item { HorizontalDivider() }

            // ── Appearance ───────────────────────────────────────────────
            item {
                Text(stringResource(R.string.settings_appearance),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            item {
                SwitchPreference(
                    title = stringResource(R.string.settings_dynamic_color),
                    checked = prefs.dynamicColor,
                    onCheckedChange = { viewModel.setDynamicColor(it) }
                )
            }
        }
    }
}

@Composable
fun SwitchPreference(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}
