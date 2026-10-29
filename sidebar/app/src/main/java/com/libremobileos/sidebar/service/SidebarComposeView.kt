package com.libremobileos.sidebar.service

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.settingslib.spa.framework.compose.rememberDrawablePainter
import com.descartes.sidebar.R
import com.libremobileos.sidebar.app.SidebarApplication
import com.libremobileos.sidebar.bean.AppInfo

@Composable
fun SidebarComposeView(
    viewModel: ServiceViewModel,
    launchApp: (AppInfo) -> Unit,
    closeSidebar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = context.getSharedPreferences(
        SidebarApplication.CONFIG,
        Context.MODE_PRIVATE
    )
    val columns = preferences.getInt("sidebar_columns", 1)
    val iconSize = preferences.getInt("sidebar_icon_size", 48)
    val iconPadding = preferences.getInt("sidebar_icon_padding", 6)
    val spacing = preferences.getInt("sidebar_column_spacing", 4)
    val cornerRadius = preferences.getFloat("sidebar_corner_radius", 30f)
    val transparency = preferences.getFloat("sidebar_background_transparency", 0.84f)
    val showShadow = preferences.getBoolean("sidebar_show_shadow", true)
    val shortcuts by viewModel.sidebarAppListFlow.collectAsState()

    val allItems = buildList {
        addAll(shortcuts.map(SidebarItem::App))
        add(SidebarItem.Divider)
        add(SidebarItem.AllApps(viewModel.allAppActivity))
        add(SidebarItem.Settings)
    }

    val cardModifier = if (showShadow) {
        modifier.shadow(12.dp, RoundedCornerShape(cornerRadius.dp))
    } else {
        modifier
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                .copy(alpha = transparency)
        ),
        shape = RoundedCornerShape(cornerRadius.dp),
        modifier = cardModifier
    ) {
        if (columns == 1) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(shortcuts) { app ->
                        SidebarItemView(
                            item = SidebarItem.App(app),
                            iconSize = iconSize,
                            iconPadding = iconPadding,
                            viewModel = viewModel,
                            launchApp = launchApp,
                            closeSidebar = closeSidebar
                        )
                    }
                }
                SidebarItemView(
                    SidebarItem.Divider, iconSize, iconPadding, viewModel, launchApp, closeSidebar
                )
                SidebarItemView(
                    SidebarItem.AllApps(viewModel.allAppActivity),
                    iconSize, iconPadding, viewModel, launchApp, closeSidebar
                )
                SidebarItemView(
                    SidebarItem.Settings, iconSize, iconPadding, viewModel, launchApp, closeSidebar
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                verticalArrangement = Arrangement.spacedBy(spacing.dp),
                modifier = Modifier.width(
                    ((iconSize + iconPadding * 2) * columns +
                        spacing * (columns - 1) + 8).dp
                )
            ) {
                items(allItems) { item ->
                    SidebarItemView(
                        item = item,
                        iconSize = iconSize,
                        iconPadding = iconPadding,
                        viewModel = viewModel,
                        launchApp = launchApp,
                        closeSidebar = closeSidebar
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarItemView(
    item: SidebarItem,
    iconSize: Int,
    iconPadding: Int,
    viewModel: ServiceViewModel,
    launchApp: (AppInfo) -> Unit,
    closeSidebar: () -> Unit
) {
    Box(
        modifier = Modifier.padding(iconPadding.dp),
        contentAlignment = Alignment.Center
    ) {
        when (item) {
            is SidebarItem.App -> Image(
                painter = rememberDrawablePainter(item.info.icon),
                contentDescription = item.info.label,
                modifier = Modifier
                    .size(iconSize.dp)
                    .clickable { launchApp(item.info) }
            )
            is SidebarItem.AllApps -> Icon(
                imageVector = Icons.Outlined.Widgets,
                contentDescription = item.info.label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size((iconSize - 4).dp)
                    .clickable { launchApp(item.info) }
            )
            SidebarItem.Settings -> Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.sidebar_settings_description),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size((iconSize - 4).dp)
                    .clickable {
                        viewModel.openSidebarSettings()
                        closeSidebar()
                    }
            )
            SidebarItem.Divider -> Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(28.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f))
            )
        }
    }
}

private sealed class SidebarItem {
    data class AllApps(val info: AppInfo) : SidebarItem()
    data class App(val info: AppInfo) : SidebarItem()
    data object Divider : SidebarItem()
    data object Settings : SidebarItem()
}
