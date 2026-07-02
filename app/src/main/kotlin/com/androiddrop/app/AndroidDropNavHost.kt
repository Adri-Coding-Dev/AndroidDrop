package com.androiddrop.app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.androiddrop.feature.diagnostics.DiagnosticsNavigation
import com.androiddrop.feature.diagnostics.DiagnosticsScreen
import com.androiddrop.feature.diagnostics.DiagnosticsViewModel
import com.androiddrop.feature.discovery.DiscoveryScreen
import com.androiddrop.feature.discovery.DiscoveryViewModel
import com.androiddrop.feature.fileexplorer.FileExplorerScreen
import com.androiddrop.feature.fileexplorer.FileExplorerViewModel
import com.androiddrop.feature.settings.SettingsNavigation
import com.androiddrop.feature.settings.SettingsScreen
import com.androiddrop.feature.settings.SettingsViewModel
import com.androiddrop.feature.transfer.TransferScreen
import com.androiddrop.feature.transfer.TransferViewModel

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AndroidDropNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem("Transferir", Icons.Default.Share, "transfer"),
        BottomNavItem("Archivos", Icons.Default.Folder, "file_explorer"),
        BottomNavItem("Descubrir", Icons.Default.Radar, "discovery"),
        BottomNavItem("Ajustes", Icons.Default.Settings, SettingsNavigation.route),
        BottomNavItem("Diagnóstico", Icons.Default.Build, DiagnosticsNavigation.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route == item.route
                    } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "transfer",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350)
                ) + fadeOut(animationSpec = tween(350))
            }
        ) {
            composable("transfer") {
                val viewModel: TransferViewModel = hiltViewModel()
                TransferScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = { navController.navigate(SettingsNavigation.route) },
                    onNavigateToDiagnostics = { navController.navigate(DiagnosticsNavigation.route) }
                )
            }
            composable("file_explorer") {
                val viewModel: FileExplorerViewModel = hiltViewModel()
                FileExplorerScreen(
                    viewModel = viewModel,
                    onFileSelected = { file ->
                        navController.navigate("transfer") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("discovery") {
                val viewModel: DiscoveryViewModel = hiltViewModel()
                DiscoveryScreen(
                    viewModel = viewModel,
                    onDeviceConnected = { device ->
                        navController.navigate("transfer") {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    }
                )
            }
            composable(SettingsNavigation.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(DiagnosticsNavigation.route) {
                val viewModel: DiagnosticsViewModel = hiltViewModel()
                DiagnosticsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
