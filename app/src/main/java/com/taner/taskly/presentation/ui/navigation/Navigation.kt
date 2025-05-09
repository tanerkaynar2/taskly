package com.taner.taskly.presentation.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.ui.screen.home.HomeScreen
import com.taner.taskly.presentation.ui.screen.add_task.AddTaskScreen
import com.taner.taskly.presentation.ui.screen.habits.AddHabitScreen
import com.taner.taskly.presentation.ui.screen.habits.HabitDetailsScreen
import com.taner.taskly.presentation.ui.screen.habits.HabitsScreen
import com.taner.taskly.presentation.ui.screen.settings.SettingsScreen
import com.taner.taskly.presentation.ui.screen.stats.StatsScreen
import com.taner.taskly.presentation.ui.screen.task_details.TaskDetailScreen
import com.taner.taskly.presentation.viewmodel.ChangeTaskColorViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.TaskViewModel

@Composable
fun Navigation(navController: NavHostController, paddingValues: PaddingValues
               , taskViewModel: TaskViewModel,
               habitViewModel: HabitViewModel, habitCheckViewModel: HabitCheckViewModel,
               changeTaskColorViewModel: ChangeTaskColorViewModel
) {

  /*  val factory = ViewModelFactory(taskRepository, habitRepository)
    val taskViewModel = ViewModelProvider(LocalContext.current as ViewModelStoreOwner,
        factory)[TaskViewModel::class.java]*/


    NavHost(navController = navController, startDestination = "home",
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = taskViewModel,
                habitViewModel = habitViewModel
            )
        }
        composable("add_task"){
            AddTaskScreen(
                navController = navController,
                viewModel = taskViewModel,
            )
        }
        composable("add_task/{taskId}/{dropdowns}",
            arguments = listOf(navArgument("taskId") {
                type = NavType.IntType;
            },navArgument("dropdowns") {
                type = NavType.StringType;
            })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            val dropdowns = backStackEntry.arguments?.getString("dropdowns")
            AddTaskScreen(
                navController = navController,
                viewModel = taskViewModel,
                editTaskId = taskId,
                dropdowns = dropdowns
            )
        }
        composable("settings"
        ) { backStackEntry ->
            SettingsScreen(
                navController = navController,
                viewModel = taskViewModel,
            )
        }
        composable("add_task/{notOn}",
            arguments = listOf(navArgument("notOn") {
                type = NavType.StringType;
            })
        ) { backStackEntry ->
            val notOn = backStackEntry.arguments?.getString("notOn")
            AddTaskScreen(
                navController = navController,
                viewModel = taskViewModel,
                enableNotification = notOn == "true"
            )
        }
        composable("habits") {
            HabitsScreen(
                navController = navController,
                viewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel
                )
        }
        composable("habit_details/{habitId}",
            arguments = listOf(navArgument("habitId") {
                type = NavType.IntType;
            })) {backStackEntry ->
            val id = backStackEntry.arguments?.getInt("habitId")
            HabitDetailsScreen(
                habitId = id?:-1,
                navController = navController,
                viewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel,
                changeTaskColorViewModel = changeTaskColorViewModel
                )
        }
        composable("add_habit") {
            AddHabitScreen(
                navController = navController,
                viewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel
                )
        }
        composable("add_habit/{id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType;
            })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            AddHabitScreen(
                navController = navController,
                viewModel = habitViewModel,
                habitCheckViewModel = habitCheckViewModel,
                editHabitId = id
            )
        }
        composable("stats") { StatsScreen(
            navController = navController,
            habitViewModel = habitViewModel,
            habitCheckViewModel = habitCheckViewModel,) }

        composable("stats/{selectedTime}",
            arguments = listOf(navArgument("selectedTime") { type = NavType.LongType })) {backStackEntry ->
            val selectedTime = backStackEntry.arguments?.getLong("selectedTime")


            StatsScreen(
                selectedTimeStamp = selectedTime,
            navController = navController,
            habitViewModel = habitViewModel,
            habitCheckViewModel = habitCheckViewModel,) }

        composable( route = "task_details/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })) {  backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            TaskDetailScreen(taskId = taskId, navController = navController, viewModel = taskViewModel, changeTaskColorViewModel = changeTaskColorViewModel)
        }
        composable( route = "task_details/{taskId}/{dayOffset}",
            arguments = listOf(
                navArgument("taskId") { type = NavType.IntType },
                navArgument("dayOffset") { type = NavType.IntType },
                ),
        ) {  backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            val dayOffset = backStackEntry.arguments?.getInt("dayOffset") ?: 0
            TaskDetailScreen(taskId = taskId, dayOffset = dayOffset, navController = navController, viewModel = taskViewModel, changeTaskColorViewModel = changeTaskColorViewModel)
        }
    }
}