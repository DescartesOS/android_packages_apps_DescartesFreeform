package com.libremobileos.sidebar.ui.all_app

import android.content.Intent
import android.content.ComponentName
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.libremobileos.sidebar.bean.AppInfo
import com.libremobileos.sidebar.app.SidebarApplication
import com.libremobileos.sidebar.ui.theme.SidebarTheme
import com.libremobileos.sidebar.utils.Logger

/**
 * @author KindBrave
 * @since 2023/10/25
 */
class AllAppActivity: ComponentActivity() {
    private val logger = Logger(TAG)
    private val viewModel: AllAppViewModel by viewModels { AllAppViewModel.Factory }

    companion object {
        private const val PACKAGE = "com.descartes.freeform"
        private const val ACTION = "com.descartes.freeform.START_FREEFORM"
        private const val TAG = "AllAppActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logger.d("onCreate")

        setContent {
            SidebarTheme {
                AllAppGridView(
                    viewModel = viewModel,
                    onClick = ::onClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        logger.d("onDestroy")
        super.onDestroy()
    }

    private fun onClick(appInfo: AppInfo) {
        val launchMode = getSharedPreferences(SidebarApplication.CONFIG, MODE_PRIVATE)
            .getInt("sidebar_launch_mode", 0)
        if (launchMode == 1) {
            runCatching {
                getSystemService(LauncherApps::class.java).startMainActivity(
                    ComponentName(appInfo.packageName, appInfo.activityName),
                    UserHandle.of(appInfo.userId),
                    null,
                    null
                )
            }.onFailure { logger.e("Failed to launch app full screen", it) }
            finish()
            return
        }

        val intent = Intent(ACTION).apply {
            setPackage(PACKAGE)
            putExtra("packageName", appInfo.packageName)
            putExtra("activityName", appInfo.activityName)
            putExtra("userId", appInfo.userId)
            putExtra("compactWindow", launchMode == 2)
        }
        sendBroadcast(intent)
        finish()
    }
}
