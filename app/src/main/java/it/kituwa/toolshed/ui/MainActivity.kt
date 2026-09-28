package it.kituwa.toolshed.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import it.kituwa.toolshed.BuildConfig
import it.kituwa.toolshed.ui.theme.ToolshedTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ToolshedTheme {
                ToolshedNavHost()
            }
        }
    }
}

@Composable
private fun ToolshedNavHost(viewModel: ShedViewModel = viewModel()) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Tapping a service from "also consider" while already looking at that
    // service would otherwise stack a duplicate destination, so back has to be
    // pressed twice to leave. Navigating to the current destination is a no-op.
    val openService: (String) -> Unit = { id ->
        if (navController.currentDestination?.route != "service/$id") {
            navController.navigate("service/$id")
        }
    }
    val openStack: (String) -> Unit = { id ->
        if (navController.currentDestination?.route != "stack/$id") {
            navController.navigate("stack/$id")
        }
    }

    NavHost(navController = navController, startDestination = "catalogue") {
        composable("catalogue") {
            CatalogueScreen(
                state = state,
                onQueryChange = viewModel::setQuery,
                onCategoryChange = viewModel::setCategory,
                onOpenService = openService,
                onOpenStacks = { navController.navigate("stacks") },
                onOpenPlanner = { navController.navigate("planner") },
                onOpenAbout = { navController.navigate("about") },
            )
        }
        composable(
            route = "service/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            val id = entry.arguments?.getString("id").orEmpty()
            ServiceDetailScreen(
                serviceId = id,
                running = id in state.shed.running,
                shortlisted = id in state.shed.shortlist,
                onToggleRunning = { viewModel.toggleRunning(id) },
                onToggleShortlist = { viewModel.toggleShortlist(id) },
                onOpenService = openService,
                onBack = { navController.popBackStack() },
            )
        }
        composable("stacks") {
            StacksScreen(
                onOpenStack = openStack,
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "stack/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            StackDetailScreen(
                stackId = entry.arguments?.getString("id").orEmpty(),
                onOpenService = openService,
                onBack = { navController.popBackStack() },
            )
        }
        composable("planner") {
            PlannerScreen(
                state = state,
                onSelectMachine = viewModel::setMachine,
                onOpenService = { navController.navigate("service/$it") },
                onClearMarks = viewModel::clearMarks,
                onBack = { navController.popBackStack() },
            )
        }
        composable("about") {
            AboutScreen(
                versionName = BuildConfig.VERSION_NAME,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
