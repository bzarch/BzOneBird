package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.archive.ArchiveScreen
import com.example.ui.screens.birds.BirdDetailScreen
import com.example.ui.screens.birds.BirdFormScreen
import com.example.ui.screens.birds.BirdsScreen
import com.example.ui.screens.birdtypes.BirdTypesScreen
import com.example.ui.screens.breeding.BreedingPairFormScreen
import com.example.ui.screens.breeding.BreedingScreen
import com.example.ui.screens.breeding.ChickFormScreen
import com.example.ui.screens.breeding.EggFormScreen
import com.example.ui.screens.cages.CageFormScreen
import com.example.ui.screens.cages.CagesScreen
import com.example.ui.screens.care.CareFormScreen
import com.example.ui.screens.care.CareScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.finance.FinanceScreen
import com.example.ui.screens.finance.TransactionFormScreen
import com.example.ui.screens.health.HealthFormScreen
import com.example.ui.screens.health.HealthScreen
import com.example.ui.screens.inventory.InventoryScreen
import com.example.ui.screens.inventory.ItemFormScreen
import com.example.ui.screens.competitions.CompetitionFormScreen
import com.example.ui.screens.competitions.CompetitionsScreen
import com.example.ui.screens.genetics.GeneticsCalculatorScreen
import com.example.ui.screens.masteran.MasteranFormScreen
import com.example.ui.screens.masteran.MasteranScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.notes.NoteFormScreen
import com.example.ui.screens.notes.NotesScreen
import com.example.ui.screens.pedigree.PedigreeCertificateScreen
import com.example.ui.screens.reminders.ReminderFormScreen
import com.example.ui.screens.reminders.RemindersScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.viewmodel.BzOneViewModel
import kotlinx.coroutines.flow.collectLatest

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Dashboard : Screen("beranda", "Beranda", Icons.Filled.Home, Icons.Outlined.Home)
    object Birds : Screen("birds", "Data Burung", Icons.Filled.Pets, Icons.Outlined.Pets)
    object Care : Screen("care", "Perawatan", Icons.Filled.EventAvailable, Icons.Outlined.EventAvailable)
    object Breeding : Screen("breeding", "Penangkaran", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder)
    object More : Screen("more", "Lainnya", Icons.Filled.Apps, Icons.Outlined.Apps)
}

@Composable
fun BzOneApp(viewModel: BzOneViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarItems = listOf(
        Screen.Dashboard,
        Screen.Birds,
        Screen.Care,
        Screen.Breeding,
        Screen.More
    )

    val showBottomBar = currentRoute in bottomBarItems.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomBarItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("bottom_nav_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // 1. Beranda / Dashboard
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToBirds = { navController.navigate(Screen.Birds.route) },
                    onNavigateToCare = { navController.navigate(Screen.Care.route) },
                    onNavigateToBreeding = { navController.navigate(Screen.Breeding.route) },
                    onNavigateToCages = { navController.navigate("cages") },
                    onNavigateToFinance = { navController.navigate("finance") },
                    onNavigateToReminders = { navController.navigate("reminders") },
                    onAddBird = { navController.navigate("bird_form?birdId=0") },
                    onAddCare = { navController.navigate("care_form?recordId=0") },
                    onAddBreeding = { navController.navigate("breeding_pair_form?pairId=0") },
                    onAddTransaction = { navController.navigate("finance_form?txId=0") },
                    onNavigateToMasteran = { navController.navigate("masteran") },
                    onNavigateToCompetitions = { navController.navigate("competitions") },
                    onNavigateToGenetics = { navController.navigate("genetics") }
                )
            }

            // 2. Data Burung
            composable(Screen.Birds.route) {
                BirdsScreen(
                    viewModel = viewModel,
                    onBirdClick = { birdId -> navController.navigate("bird_detail/$birdId") },
                    onAddBirdClick = { navController.navigate("bird_form?birdId=0") },
                    onEditBirdClick = { birdId -> navController.navigate("bird_form?birdId=$birdId") }
                )
            }

            composable(
                route = "bird_detail/{birdId}",
                arguments = listOf(navArgument("birdId") { type = NavType.LongType })
            ) { backStackEntry ->
                val birdId = backStackEntry.arguments?.getLong("birdId") ?: 0L
                BirdDetailScreen(
                    birdId = birdId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditBird = { id -> navController.navigate("bird_form?birdId=$id") },
                    onNavigateToPedigree = { id -> navController.navigate("pedigree?birdId=$id") }
                )
            }

            composable(
                route = "bird_form?birdId={birdId}",
                arguments = listOf(navArgument("birdId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val birdId = backStackEntry.arguments?.getLong("birdId") ?: 0L
                BirdFormScreen(
                    birdId = birdId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 3. Perawatan
            composable(Screen.Care.route) {
                CareScreen(
                    viewModel = viewModel,
                    onAddCareClick = { navController.navigate("care_form?recordId=0") },
                    onEditCareClick = { recId -> navController.navigate("care_form?recordId=$recId") }
                )
            }

            composable(
                route = "care_form?recordId={recordId}",
                arguments = listOf(navArgument("recordId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                CareFormScreen(
                    recordId = recordId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 4. Penangkaran
            composable(Screen.Breeding.route) {
                BreedingScreen(
                    viewModel = viewModel,
                    onAddPairClick = { navController.navigate("breeding_pair_form?pairId=0") },
                    onEditPairClick = { pairId -> navController.navigate("breeding_pair_form?pairId=$pairId") },
                    onAddEggClick = { navController.navigate("egg_form?eggId=0") },
                    onEditEggClick = { eggId -> navController.navigate("egg_form?eggId=$eggId") },
                    onAddChickClick = { navController.navigate("chick_form?chickId=0") },
                    onEditChickClick = { chickId -> navController.navigate("chick_form?chickId=$chickId") }
                )
            }

            composable(
                route = "breeding_pair_form?pairId={pairId}",
                arguments = listOf(navArgument("pairId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val pairId = backStackEntry.arguments?.getLong("pairId") ?: 0L
                BreedingPairFormScreen(
                    pairId = pairId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "egg_form?eggId={eggId}",
                arguments = listOf(navArgument("eggId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val eggId = backStackEntry.arguments?.getLong("eggId") ?: 0L
                EggFormScreen(
                    eggId = eggId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "chick_form?chickId={chickId}",
                arguments = listOf(navArgument("chickId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val chickId = backStackEntry.arguments?.getLong("chickId") ?: 0L
                ChickFormScreen(
                    chickId = chickId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // 5. Lainnya (Hub)
            composable(Screen.More.route) {
                MoreScreen(
                    onNavigate = { destination -> navController.navigate(destination) }
                )
            }

            // Kandang
            composable("cages") {
                CagesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddCageClick = { navController.navigate("cage_form?cageId=0") },
                    onEditCageClick = { id -> navController.navigate("cage_form?cageId=$id") }
                )
            }

            composable(
                route = "cage_form?cageId={cageId}",
                arguments = listOf(navArgument("cageId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val cageId = backStackEntry.arguments?.getLong("cageId") ?: 0L
                CageFormScreen(
                    cageId = cageId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Stok Pakan & Perlengkapan
            composable("inventory") {
                InventoryScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddItemClick = { navController.navigate("inventory_form?itemId=0") },
                    onEditItemClick = { id -> navController.navigate("inventory_form?itemId=$id") }
                )
            }

            composable(
                route = "inventory_form?itemId={itemId}",
                arguments = listOf(navArgument("itemId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getLong("itemId") ?: 0L
                ItemFormScreen(
                    itemId = itemId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Keuangan
            composable("finance") {
                FinanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddTransactionClick = { navController.navigate("finance_form?txId=0") },
                    onEditTransactionClick = { id -> navController.navigate("finance_form?txId=$id") }
                )
            }

            composable(
                route = "finance_form?txId={txId}",
                arguments = listOf(navArgument("txId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val txId = backStackEntry.arguments?.getLong("txId") ?: 0L
                TransactionFormScreen(
                    txId = txId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Kesehatan
            composable("health") {
                HealthScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddRecordClick = { navController.navigate("health_form?recordId=0") },
                    onEditRecordClick = { id -> navController.navigate("health_form?recordId=$id") }
                )
            }

            composable(
                route = "health_form?recordId={recordId}",
                arguments = listOf(navArgument("recordId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                HealthFormScreen(
                    recordId = recordId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pengingat
            composable("reminders") {
                RemindersScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddReminderClick = { navController.navigate("reminder_form?reminderId=0") },
                    onEditReminderClick = { id -> navController.navigate("reminder_form?reminderId=$id") }
                )
            }

            composable(
                route = "reminder_form?reminderId={reminderId}",
                arguments = listOf(navArgument("reminderId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val reminderId = backStackEntry.arguments?.getLong("reminderId") ?: 0L
                ReminderFormScreen(
                    reminderId = reminderId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Catatan
            composable("notes") {
                NotesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onAddNoteClick = { navController.navigate("note_form?noteId=0") },
                    onEditNoteClick = { id -> navController.navigate("note_form?noteId=$id") }
                )
            }

            composable(
                route = "note_form?noteId={noteId}",
                arguments = listOf(navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
                NoteFormScreen(
                    noteId = noteId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Jenis Burung
            composable("bird_types") {
                BirdTypesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Laporan
            composable("reports") {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Arsip / Trash
            composable("archive") {
                ArchiveScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Pengaturan
            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Audio Masteran Burung
            composable("masteran") {
                MasteranScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToForm = { id -> navController.navigate("masteran_form?masteranId=$id") }
                )
            }

            composable(
                route = "masteran_form?masteranId={masteranId}",
                arguments = listOf(navArgument("masteranId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val masteranId = backStackEntry.arguments?.getLong("masteranId") ?: 0L
                MasteranFormScreen(
                    masteranId = masteranId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Prestasi Lomba & Riwayat Gantangan
            composable("competitions") {
                CompetitionsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToForm = { id -> navController.navigate("competition_form?recordId=$id") }
                )
            }

            composable(
                route = "competition_form?recordId={recordId}",
                arguments = listOf(navArgument("recordId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: 0L
                CompetitionFormScreen(
                    recordId = recordId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Kalkulator Genetika
            composable("genetics") {
                GeneticsCalculatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Sertifikat Silsilah (Pedigree)
            composable(
                route = "pedigree?birdId={birdId}",
                arguments = listOf(navArgument("birdId") {
                    type = NavType.LongType
                    defaultValue = 0L
                })
            ) { backStackEntry ->
                val birdId = backStackEntry.arguments?.getLong("birdId") ?: 0L
                PedigreeCertificateScreen(
                    initialBirdId = birdId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
