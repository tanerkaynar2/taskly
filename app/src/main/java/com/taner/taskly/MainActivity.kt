package com.taner.taskly

import android.app.ComponentCaller
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material3.*
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.taner.taskly.core.utils.DateUtils.Companion.dateFormat2
import com.taner.taskly.data.local.AppDatabase
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.repository.HabitRepository
import com.taner.taskly.data.repository.TaskRepository
import com.taner.taskly.presentation.ui.components.BottomBar
import com.taner.taskly.presentation.ui.navigation.Navigation
import com.taner.taskly.presentation.ui.theme.TasklyTheme
import com.taner.taskly.presentation.viewmodel.ChangeTaskColorViewModel
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import com.taner.taskly.presentation.viewmodel.factory.ViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.util.Date
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.lifecycleScope
import com.taner.taskly.core.utils.BackupUtils
import com.taner.taskly.core.utils.DateUtils.Companion.getDayOfYearAndDaYear
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.data.local.dao.NotificationDao
import com.taner.taskly.data.local.entity.HabitEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.repository.HabitCheckRepository
import com.taner.taskly.data.repository.NotificationRepository
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.presentation.habit.HabitCheckViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.dayOfYear
import com.taner.taskly.presentation.viewmodel.HabitViewModel.Companion.year
import com.taner.taskly.presentation.viewmodel.NotificationViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : ComponentActivity() {


    companion object{
        val screens = listOf("home", "add_task", "habits", "stats", "task_details", "settings", "add_habit")
        val screenNames = listOf("", "Yeni Görev", "Rutinler", "Verilerim","t", "Ayarlar","Rutin ekle")

        const val REQUEST_CODE_READ_STORAGE = 1
        const val REQUEST_CODE_PICK_ZIP = 2

        const val kac_saatte_bir_arkaplan_servisi_bildirimler_icin = 3

        var defaultRepetitionData: Int=TaskRepetition.NONE.ordinal
        var defaultCategoryData: String?=null
        var defaultCategoryDetailData: String?=null
        var defaultTaskOrder: Boolean?=null
        var defaultTimeScope: Boolean?=true

        var isEnabledAnimation: Boolean=true

        var grantedCallback:()->Unit = {}

        var themeMode = 0 // 0 Dark 1 Light
        var isSystemTheme = false // 0 Dark 1 Light
        var isDarkTaskCard = false // 0 Dark 1 Light



        var defaultScreenLayout = "İstatistik, Görev Takvimi, Alışkanlık listesi"
        var homeScreenLayout = defaultScreenLayout

        var notViewModel: NotificationViewModel?=null

        val reminderAlarmManagerId = 1001
        val dailyRememberNotificationID = 1002
        val dailyRememberNotificationChannelId = "daily_remember_1003"
        val channelId = "task_reminder_channel"
        val channelName = "Task Reminders"
        val importance = NotificationManager.IMPORTANCE_HIGH
        var isNotMute = true

        var isSyncEnabled = false
        fun saveLastSYNC(sp: SharedPreferences,c:Context){
            sp.edit().putString("LAST_SYNC", dateFormat2.format(Date())).apply()
        }

        fun isDarkTheme():Boolean{
            return themeMode == 0
        }

        fun isLightTheme():Boolean{
            return themeMode == 1
        }

        fun getTheme(sp: SharedPreferences): Int?{
           return sp.getInt("app_theme_mode",-1).takeIf { it>-1 }
        }
        fun setTheme(sp: SharedPreferences, theme: Int?){
            isSystemTheme = theme == null
            if(theme!=null){
                sp.edit().putInt("app_theme_mode", theme).apply()
            }else sp.edit().remove("app_theme_mode").apply()
        }
        fun applyDarkTheme(sp: SharedPreferences){
            isSystemTheme = false
            sp.edit().putInt("app_theme_mode", 0).apply()
        }
        fun applyLightTheme(sp: SharedPreferences){
            isSystemTheme = false
            sp.edit().putInt("app_theme_mode", 1).apply()
        }
        fun applySystemTheme(sp: SharedPreferences){
            isSystemTheme = true
            sp.edit().remove("app_theme_mode").apply()
        }
    }

    lateinit var db: AppDatabase

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Channel for task reminder notifications"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }




        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_db"
        ).build()

        val taskDao = db.taskDao()
        val habitDao = db.habitDao()
        val habitCheckDao = db.habitCheckDao()
        val notificationDao = db.notificationDao()

        val taskRepository = TaskRepository(taskDao)
        val habitRepository = HabitRepository(habitDao,habitCheckDao)
        val habitCheckRepository = HabitCheckRepository(habitCheckDao,habitDao)
        val notificationRepository = NotificationRepository(notificationDao)

        val factory = ViewModelFactory(taskRepository, habitRepository, habitCheckRepository, notificationRepository)
        val taskViewModel = ViewModelProvider(viewModelStore,
            factory)[TaskViewModel::class.java]
        val habitViewModel = ViewModelProvider(viewModelStore,
            factory)[HabitViewModel::class.java]
        val habitCheckViewModel = ViewModelProvider(viewModelStore,
            factory)[HabitCheckViewModel::class.java]
        val notificationViewModel = ViewModelProvider(viewModelStore,
            factory)[NotificationViewModel::class.java]
        notViewModel = notificationViewModel

        fun isSystemInDarkMode(): Boolean {
            val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }

        Companion.getTheme(getSharedPreferences(packageName, MODE_PRIVATE)).let{
            if(it==null){
                themeMode = if(isSystemInDarkMode()) 0 else 1
            }else themeMode = it

            isSystemTheme = (it==null)

        }
        val sp = getSharedPreferences(packageName, MODE_PRIVATE)
        sp.getString("homeScreenLayout", defaultScreenLayout)?.let {
            homeScreenLayout = it
        }
        setContent {
            TasklyTheme(themeMode) {

                habitViewModel.loadHabits(loadHabitChecks = true,getTotalCount = true, All = false)




                val values = getDayOfYearAndDaYear()
                dayOfYear = values.first
                year = values.second


                fun getLastSYNC(sp: SharedPreferences): String? {
                    return sp.getString("LAST_SYNC",  null)
                }

                val last = getLastSYNC(sp)
                isSyncEnabled = last!=dateFormat2.format(Date())

                val sharedViewModel: ChangeTaskColorViewModel = viewModel()


                val systemUiController = rememberSystemUiController()

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val taskId = navBackStackEntry?.arguments?.getInt("taskId")
                val task by produceState<TaskEntity?>(initialValue = null, taskId) {
                    value = if (taskId != null) taskViewModel.getTaskById(taskId) else null
                }
                val habitId = navBackStackEntry?.arguments?.getInt("habitId")
                val habit by produceState<HabitEntity?>(initialValue = null, habitId) {
                    value = if (habitId != null) habitViewModel.getHabitById(habitId) else null
                }

                val title = try { screens.indexOf(currentRoute).let { screenNames.getOrNull(it)?: "" } } catch (e: Exception) { null }

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color =Color.Transparent//(0xFF313131)
                    )
                }
                Scaffold(

                    topBar = {
                        TopAppBar(
                            title = { Text(title?.let{if(currentRoute?.startsWith("habit_details")==true){
                                task?.title?: "Alışkanlık detayları"
                            }else if(currentRoute?.startsWith("task_details")==true){
                                task?.title?: "Görev detayları"
                            } else if(it=="") "Bugün  /  ${

                                dateFormat2.format(Calendar.getInstance().time)
                                
                            }" else it} ?: "", color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.fillMaxWidth()) },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = if(isLightTheme() && currentRoute?.startsWith("task_details")==true) Color.White.copy(0.6f) else Color.Transparent/*(0xFF313131)*/,
                                titleContentColor = MaterialTheme.colorScheme.onSecondary ,
                                actionIconContentColor= MaterialTheme.colorScheme.onSecondary ,
                                navigationIconContentColor = MaterialTheme.colorScheme.onSecondary
                            ), modifier = Modifier.fillMaxWidth()
                            ,
                            navigationIcon = {
                                val canGoBack = navController.previousBackStackEntry != null
                                if (canGoBack){
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(Icons.Default.ArrowBackIos, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onSecondary
                                            , modifier = Modifier.size(36.dp))
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                            },
                            actions = {
                                if (currentRoute == "home") {
                                    IconButton(onClick = { navController.navigate("add_task") }) {
                                        Icon(Icons.Default.Add, contentDescription = "Görev Ekle", tint = MaterialTheme.colorScheme.onSecondary
                                        , modifier = Modifier.size(36.dp))
                                    }

                                    Spacer(Modifier.width(16.dp))

                                    IconButton(onClick = { navController.navigate("settings") }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = MaterialTheme.colorScheme.onSecondary
                                        , modifier = Modifier.size(36.dp))
                                    }

                                    Spacer(Modifier.width(16.dp))
                                }
                                if (currentRoute?.startsWith("task_details") == true) {

                                    SideEffect {
                                        val baseColor = task?.color?.let { Color(it) }
                                        val lightColor = baseColor?.copy(alpha = 0.3f)
                                        systemUiController.setStatusBarColor(
                                            color = lightColor?: Color(0xFF313131)
                                        )
                                    }

                                    val baseColor = task?.color?.let { Color(it) }
                                    baseColor?.let { baseColor->
                                        Box(Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(baseColor)
                                            .clickable {


                                                CoroutineScope(Dispatchers.Main).launch {
                                                    sharedViewModel.triggerRefresh()
                                                }

                                            }){
                                        }

                                        Spacer(Modifier.width(24.dp))
                                    }
                                }
                                if (currentRoute?.startsWith("habit_details") == true) {

                                    SideEffect {
                                        val baseColor = habit?.color?.let { Color(it) }
                                        val lightColor = baseColor?.copy(alpha = 0.3f)
                                        systemUiController.setStatusBarColor(
                                            color = lightColor?: Color(0xFF313131)
                                        )
                                    }

                                    val baseColor = habit?.color?.let { Color(it) }
                                    baseColor?.let { baseColor->
                                        Box(Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(baseColor)
                                            .clickable {


                                                CoroutineScope(Dispatchers.Main).launch {
                                                    sharedViewModel.triggerRefreshForHabit()
                                                }

                                            }){
                                        }
                                        habitId?.let{
                                            Spacer(Modifier.width(8.dp))
                                            Box(Modifier
                                                .width(32.dp)
                                                .padding(horizontal = 4.dp)
                                                .clip(CircleShape)
                                                .clickable {


                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        sharedViewModel.triggerRefreshForHabitDelDialog()
                                                    }

                                                }){

                                                Image(  Icons.Default.Delete, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                                    modifier = Modifier.fillMaxSize()
                                                    , contentDescription = "")

                                            }
                                            Spacer(Modifier.width(8.dp))
                                            Box(Modifier
                                                .width(32.dp)
                                                .padding(horizontal = 4.dp)
                                                .clip(CircleShape)
                                                .clickable {


                                                    CoroutineScope(Dispatchers.Main).launch {
                                                        sharedViewModel.triggerRefreshForHabitEditDialog()
                                                    }

                                                }){

                                                Image(  Icons.Default.Edit, colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                                    modifier = Modifier.fillMaxSize()
                                                    , contentDescription = "")

                                            }
                                        }

                                        Spacer(Modifier.width(24.dp))
                                    }
                                }
                            }
                        )
                    },

                    bottomBar = {
                        if (currentRoute in screens) {
                            BottomBar(navController)
                        }
                    }
                ) {paddingValues ->
                    Navigation(navController = navController, paddingValues = paddingValues
                    , taskViewModel = taskViewModel,
                        habitViewModel = habitViewModel, habitCheckViewModel = habitCheckViewModel,
                        changeTaskColorViewModel = sharedViewModel)
                }


                LaunchedEffect(Unit) {





                    val lastSetTime = sp.getLong("is_setted_midnight_receiver", 0L)
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastSetTime > 5 * 60 * 60 * 1000) { // 5 saat
                        NotificationUtils.setReminderAlarm(this@MainActivity)

                    }

                    sp.getString("defaultCategory","null").toString().let{
                        if(it!="null"){

                            sp.getString("defaultCategory","null").toString().let{
                                if(it!="null"){
                                    defaultCategoryData = it
                                    if(it == "En Son"){



                                        lifecycleScope.launch {
                                            taskViewModel.getLastTask()?.let{
                                                it.category?.let{c->
                                                    if(it.category == TaskCategory.CUSTOM){
                                                        it.customCategoryDetail?.let{defaultCategoryDetailData = it.takeIf { it!="" }}
                                                    }else{
                                                        defaultCategoryDetailData = c.turkishName.takeIf { it!="" }
                                                    }
                                                }
                                            }
                                        }


                                    }
                                }
                            }

                            sp.getString("defaultTaskOrder","null").toString().let{
                                it.toBooleanStrictOrNull().let {
                                    defaultTaskOrder = it!=false
                                }
                            }

                            sp.getString("defaultTimeScope","null").toString().let{
                                it.toBooleanStrictOrNull().let {
                                    defaultTimeScope = it
                                }
                            }

                        }
                    }

                    sp.getInt("defaultRepetition",TaskRepetition.NONE.ordinal).let{
                        defaultRepetitionData = it
                    }
                    sp.getBoolean("isEnabledAnimation",true).let{
                        isEnabledAnimation = it
                    }


                    sp.getBoolean("isDarkTaskCard",false).let {
                        isDarkTaskCard = it
                    }
                    sp.getBoolean("isNotificationMute",true).let {
                        isNotMute = it
                    }








                    lifecycleScope.launch {

                        val twoDaysAgo = Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY,0)

                            add(Calendar.DAY_OF_YEAR, -2)
                        }.timeInMillis
                        taskDao.deleteTasksOlderThanTwoDays(twoDaysAgo)




                        delay(700)

                        intent.extras?.getInt("taskId")?.let{
                            if(it>-1){

                                taskViewModel.getTaskById(it)?.let {

                                    withContext(Dispatchers.Main){
                                        val task = it.toDomain()


                                        navController.navigate("task_details/${task.id}")

                                    }

                                }

                            }
                        }

                        intent.extras?.getInt("habitId")?.let{
                            if(it>-1){

                                habitViewModel.getHabitById(it)?.let {

                                    withContext(Dispatchers.Main){
                                        val habit = it.toDomain()


                                        navController.navigate("habit_details/${habit.id}")

                                    }

                                }

                            }
                        }

                    }
                }

            }




        }
    }



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_READ_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    grantedCallback.invoke()
                } else {
                    // İzin verilmedi

                }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)




        if (requestCode == REQUEST_CODE_PICK_ZIP && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                db.close()
                BackupUtils().importAppDataFromZip(this, uri)
            }
        }
    }

}

