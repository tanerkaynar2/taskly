package com.taner.taskly.presentation.ui.screen.settings

import android.app.Activity
import android.app.Activity.MODE_PRIVATE
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import com.taner.taskly.MainActivity
import com.taner.taskly.MainActivity.Companion
import com.taner.taskly.MainActivity.Companion.defaultCategoryData
import com.taner.taskly.MainActivity.Companion.defaultRepetitionData
import com.taner.taskly.MainActivity.Companion.defaultScreenLayout
import com.taner.taskly.MainActivity.Companion.homeScreenLayout
import com.taner.taskly.MainActivity.Companion.isDarkTaskCard
import com.taner.taskly.MainActivity.Companion.isDarkTheme
import com.taner.taskly.MainActivity.Companion.isEnabledAnimation
import com.taner.taskly.MainActivity.Companion.isLightTheme
import com.taner.taskly.MainActivity.Companion.isNotMute
import com.taner.taskly.MainActivity.Companion.isSystemTheme
import com.taner.taskly.MainActivity.Companion.notViewModel
import com.taner.taskly.MainActivity.Companion.setTheme
import android.Manifest
import android.net.Uri
import androidx.core.app.ActivityCompat.startActivityForResult
import com.taner.taskly.MainActivity.Companion.REQUEST_CODE_PICK_ZIP
import com.taner.taskly.MainActivity.Companion.REQUEST_CODE_READ_STORAGE
import com.taner.taskly.core.utils.BackupUtils
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.core.utils.NotificationUtils
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.presentation.viewmodel.TaskViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import androidx.core.net.toUri
import com.taner.taskly.AboutActivity
import com.taner.taskly.PrivacyPolicyActivity

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: TaskViewModel,
) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        val context = LocalContext.current

        val calendar = remember { Calendar.getInstance() }

        val sp = remember { context.getSharedPreferences(context.packageName, MODE_PRIVATE) }

        val dailyReminderTime = remember { mutableStateOf<String?>("08:00") }
        val reminderOffsetMinutes = remember { mutableStateOf<Int?>(15) }
        val pastTaskReminder = remember { mutableStateOf<Boolean>(true) }
        val nowTaskReminder = remember { mutableStateOf<Boolean>(true) }
        val habitReminder = remember { mutableStateOf<Boolean>(true) }
        val notify_on_missed_habit = remember { mutableStateOf<Boolean>(true) }
        val delayTime = remember { mutableStateOf<Int>(60) }
        val defaultTimeScope = remember { mutableStateOf<Boolean?>(true) }
        val defaultTaskOrder = remember { mutableStateOf<Boolean>(true) }
        val isNotificationMute = remember { mutableStateOf<Boolean>(isNotMute) }
        val defaultCategory = remember { mutableStateOf<String>(TaskCategory.DAILY.turkishName) }
        val defaultRepetition = remember { mutableStateOf(defaultRepetitionData) }
        val isNotEnabled = remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            sp.getString("dailyReminderTime",null).let{
                dailyReminderTime.value = it
            }
            sp.getInt("reminderOffsetMinutes",-1).takeIf { it>-1 }.let{
                reminderOffsetMinutes.value = it
            }
            sp.getInt("delayTime",-1).takeIf { it>-1 }?.let{
                delayTime.value = it
            }
            sp.getBoolean("pastTaskReminder",false).let{
                pastTaskReminder.value = it
            }
            sp.getBoolean("habitReminder",true).let{
                habitReminder.value = it
            }
            sp.getBoolean("notify_on_missed_habit",true).let{
                notify_on_missed_habit.value = it
            }
            sp.getBoolean("nowTaskReminder",false).let{
                nowTaskReminder.value = it
            }

            sp.getString("defaultTimeScope","null").toString().let{
                it.toBooleanStrictOrNull().let {
                    defaultTimeScope.value = it
                }
            }
            sp.getString("defaultTaskOrder","null").toString().let{
                it.toBooleanStrictOrNull().let {
                    defaultTaskOrder.value = it!=false
                }
            }
            sp.getString("defaultCategory",TaskCategory.DAILY.turkishName).toString().let{
                defaultCategory.value = it
            }
            sp.getBoolean("isNotEnabled",false).let{
                isNotEnabled.value = it
            }
        }

        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val format = timeFormat.format(calendar.timeInMillis)
                sp.edit().putString("dailyReminderTime",format).apply()

                dailyReminderTime.value = format



                dailyReminderTime.value?.let { NotificationUtils.dailyRemember(context, it) }

            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )




        fun disableNotifications(){
            dailyReminderTime.value = null
            sp.edit().remove("dailyReminderTime").apply()

            NotificationUtils.cancelScheduledNotification(context,MainActivity.dailyRememberNotificationID)

            sp.edit().remove("reminderOffsetMinutes").apply()
            reminderOffsetMinutes.value = null

            pastTaskReminder.value = false
            habitReminder.value = false
            notify_on_missed_habit.value = false
            nowTaskReminder.value = false

            sp.edit().putBoolean("pastTaskReminder",false).apply()
            sp.edit().putBoolean("nowTaskReminder",false).apply()
            sp.edit().putBoolean("habitReminder",false).apply()
            sp.edit().putBoolean("notify_on_missed_habit",false).apply()


        }



        // Genel Ayarlar
        SettingSection(title = "Genel") {

            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded = false }
            ) {

                val list = mutableListOf("Kapalı","08:00","12:00","13:00","17:00","20:00","SEÇ")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {


                            if(index==list.lastIndex){
                                timePickerDialog.show()
                            }else if(index == 0){
                                dailyReminderTime.value = null
                                sp.edit().remove("dailyReminderTime").apply()

                                NotificationUtils.cancelScheduledNotification(context,MainActivity.dailyRememberNotificationID)

                            }else{
                                dailyReminderTime.value = item

                                sp.edit().putString("dailyReminderTime",item).apply()
                                dailyReminderTime.value?.let { NotificationUtils.dailyRemember(context, it) }
                            }

                            expanded = false
                        }
                    )
                }
            }

            SettingItem(title = "Günlük Hatırlatıcı Saati", value = dailyReminderTime.value?: "Kapalı"){



                expanded = true

            }

            var expanded2 by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded2,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded2 = false }
            ) {

                val list = mutableListOf("Tümü","Geçmiş","Gelecek")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            if(index==0){
                                defaultTimeScope.value = null
                                sp.edit().putString("defaultTimeScope","null").apply()
                            }else{
                                defaultTimeScope.value = index==2
                                sp.edit().putString("defaultTimeScope","${defaultTimeScope.value}").apply()

                            }
                            expanded2 = false
                        }
                    )
                }
            }
            SettingItem(title = "Varsayılan Görev Gösterim Zamanı", if(defaultTimeScope.value==null) "Tümü" else if(defaultTimeScope.value==true)
            "Gelecek" else "Geçmiş"){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {
                    expanded2 = true
                }


            }

            var expanded3 by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded3,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded3 = false }
            ) {

                val list = mutableListOf("Zaman","Öncelik")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            defaultTaskOrder.value = index == 0

                            MainActivity.defaultTaskOrder = defaultTaskOrder.value

                            sp.edit().putString("defaultTaskOrder","${defaultTaskOrder.value}").apply()
                            expanded3 = false
                        }
                    )
                }
            }
            SettingItem(title = "Varsayılan Görev Listeleme Sırası", if(defaultTaskOrder.value) "Zaman" else "Öncelik"){
                expanded3 = true
            }
            var expanded4 by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded4,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded4 = false }
            ) {

                val list = mutableListOf<String>()
                TaskCategory.entries.forEach {
                    (if(it == TaskCategory.CUSTOM){
                        "En Son"
                    }else it.turkishName).let{
                        list.add(it)
                    }
                }

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            defaultCategoryData = item


                            defaultCategory.value = item
                            sp.edit().putString("defaultCategory","${defaultCategory.value}").apply()

                            expanded4 = false
                        }
                    )
                }
            }
            SettingItem(title = "Varsayılan Kategori", value = defaultCategory.value){
                expanded4 = true
            }
            var expanded5 by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded5,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded5 = false }
            ) {

                val list = mutableListOf<String>()
                TaskRepetition.entries.forEach {
                    list.add(it.turkishName)
                }

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            defaultRepetition.value = index
                            defaultRepetitionData = index


                            sp.edit().putInt("defaultRepetition",defaultRepetition.value).apply()

                            expanded5 = false
                        }
                    )
                }
            }
            SettingItem(title = "Varsayılan Tekrar Durumu", value = TaskRepetition.entries.get(
                defaultRepetition.value).turkishName){
                expanded5 = true
            }
        }


        Divider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp))
        Spacer(Modifier.height(16.dp))

        // Bildirimler

        SettingSection(title = "Bildirimler") {
            SettingSwitchItem(title = "Bildirimler Aktif", isChecked = isNotEnabled.value){b->


                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {
                    isNotEnabled.value = b

                    if(b){



                        dailyReminderTime.value =  "08:00"

                        dailyReminderTime.value?.let { NotificationUtils.dailyRemember(context, it) }

                        var min = 15

                        reminderOffsetMinutes.value = min


                        pastTaskReminder.value = true
                        nowTaskReminder.value = true
                        habitReminder.value = true
                        notify_on_missed_habit.value = true


                        NotificationUtils.enableNotification(sp,dailyReminderTime.value!!,min)
                    }else{
                        sp.edit().putBoolean("isNotEnabled",b).apply()
                        disableNotifications()
                    }
                }



            }


            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded = false }
            ) {

                val list = mutableListOf("kapalı","5","10","15","20","30","1 saat","2 saat","3 saat","4 saat")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            if(index == 0){
                                sp.edit().remove("reminderOffsetMinutes").apply()
                                reminderOffsetMinutes.value = null
                            }else{
                                var min = item.toIntOrNull()?: item.replace(" saat","").toInt().let{it * 60}

                                reminderOffsetMinutes.value = min

                                sp.edit().putInt("reminderOffsetMinutes",min).apply()

                            }

                            expanded = false
                        }
                    )
                }
            }

            SettingItem(title = "Görev Öncesi Hatırlatma", value = "${reminderOffsetMinutes.value?.let{if(it>59) "" else it}?: "Kapalı"} ${if(reminderOffsetMinutes.value == 0 || reminderOffsetMinutes.value==null) {
                ""
            }else if((reminderOffsetMinutes.value ?: 0) > 59) ((reminderOffsetMinutes.value?:0) / 60 ).toString() 
                    + " saat önce" else " dakika önce"}"){
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {
                    expanded = true
                }



            }


            var expanded2 by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded2,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded2 = false }
            ) {

                val list = mutableListOf("15","30","45","1 saat","2 saat")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {
                            var min = item.toIntOrNull()?: item.replace(" saat","").toInt().let{it * 60}

                            delayTime.value = min

                            sp.edit().putInt("delayTime",min).apply()



                            expanded2 = false
                        }
                    )
                }
            }
            SettingItem(title = "Varsayılan Erteleme Süresi",delayTime.value.let{
                if(it>59) it / 60 else it
            }.toString() + if(delayTime.value > 59) " saat" else " dk"){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {

                    expanded2 = true
                }

            }
            SettingSwitchItem(title = "Geçmiş Görevler", isChecked = pastTaskReminder.value){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {
                    pastTaskReminder.value = it

                    sp.edit().putBoolean("pastTaskReminder",it).apply()
                }



            }
            SettingSwitchItem(title = "Görev zamanı bildirimi", isChecked = nowTaskReminder.value){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {

                    nowTaskReminder.value = it

                    sp.edit().putBoolean("nowTaskReminder",it).apply()
                }


            }
            SettingSwitchItem(title = "Alışkanlıklar bildirimi", isChecked = nowTaskReminder.value){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {


                    habitReminder.value = it

                    sp.edit().putBoolean("habitReminder",it).apply()
                }

            }
            SettingSwitchItem(title = "“Alışkanlık aksatıldığında bildirim gönder", isChecked = nowTaskReminder.value){

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1 // İzin isteği kodu
                    )
                }else {


                    notify_on_missed_habit.value = it

                    sp.edit().putBoolean("notify_on_missed_habit",it).apply()
                }

            }

            val isChecked = remember { mutableStateOf(!isNotificationMute.value) }
            SettingSwitchItem("Bildirim sesi",isChecked.value) {checked ->
                isNotificationMute.value = !checked
                isChecked.value = checked
                isNotMute = !checked
                sp.edit().putBoolean("isNotificationMute",!checked).apply()
            }

        }
        Divider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp))
        Spacer(Modifier.height(16.dp))

        SettingSection(title = "Ana Sayfa & Navigasyon") {


            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded = false }
            ) {

                fun <T> List<T>.permutations(): List<List<T>> {
                    if (this.size <= 1) return listOf(this)
                    val result = mutableListOf<List<T>>()
                    for (i in this.indices) {
                        val element = this[i]
                        val rest = this.take(i) + this.drop(i + 1)
                        for (perm in rest.permutations()) {
                            result.add(listOf(element) + perm)
                        }
                    }
                    return result
                }

                val list = homeScreenLayout.split(",").permutations().map { it.map { it.trim() }.joinToString(", ") }
                    .let {
                        it.toMutableList().let{
                            it.add(0,"   Varsayılan")
                            it.toList()
                        }
                    }


                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            var item = if(item!="   Varsayılan") item else defaultScreenLayout

                            homeScreenLayout = item
                            sp.edit().putString("homeScreenLayout",item).apply()


                            expanded = false
                        }
                    )
                }
            }


            SettingItem(title = "Ana Sayfa düzeni", value = if(homeScreenLayout == defaultScreenLayout) "Varsayılan" else
                homeScreenLayout.substringBefore(",") + homeScreenLayout.substringAfter(",").let{", ${it.take(4)}."}
            + homeScreenLayout.substringAfter(",").substringAfter(",").let { ", ${it.take(4)}. ..." }){

                expanded = true

            }
            val isCheck = remember { mutableStateOf(isEnabledAnimation) }
            SettingSwitchItem (title = "Animasyon",isCheck.value ){
                isEnabledAnimation = it
                isCheck.value = it
                sp.edit().putBoolean("isEnabledAnimation",it).apply()
            }
        }

        // Tema ve Görünüm
        SettingSection(title = "Tema ve Görünüm") {




            var expanded by remember { mutableStateOf(false) }
            DropdownMenu(
                expanded = expanded,
                modifier = Modifier.align(Alignment.End),
                onDismissRequest = { expanded = false }
            ) {

                val list = mutableListOf("Koyu","Aydınlık","Sistem")

                list.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        text = { Text(item.toString(), color = MaterialTheme.colorScheme.onSecondary) },
                        onClick = {

                            setTheme(sp,index.takeIf { it!=list.lastIndex })

                            expanded = false

                            val activity = context as? Activity

                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                            activity?.finish()

                        }
                    )
                }
            }

            SettingItem(title = "Tema Modu", value = if(isSystemTheme){
                "Sistem" }
            else if(isLightTheme()) {
                "Aydınlık"
            }else{
                "Koyu"
            }
            ){
                expanded = true
            }

            var switchItemChecker by remember { mutableStateOf<Boolean>(isDarkTaskCard) }
            SettingSwitchItem ("Koyu Görev Kartları",switchItemChecker){
                isDarkTaskCard = it
                sp.edit().putBoolean("isDarkTaskCard",it).apply()

                switchItemChecker = it
            }
        }
        Divider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp))


        var showTasksDia by remember { mutableStateOf(false) }
        if(showTasksDia){
            ShowAllTasksDialog(viewModel,navController){
                showTasksDia = false
            }
        }


        val ConfirmDeleteAllTasksDialog = remember { mutableStateOf(false) }
        if(ConfirmDeleteAllTasksDialog.value){
            AlertDialog(
                onDismissRequest = {ConfirmDeleteAllTasksDialog.value = false},
                title = { Text("Tüm Görevleri Sil", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                    MaterialTheme.colorScheme.onSecondary) },
                text = { Text("Bu işlem geri alınamaz. Tüm görevleri silmek istediğine emin misin?",color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                    MaterialTheme.colorScheme.onSecondary) },
                confirmButton = {
                    Button(onClick = {


                        ConfirmDeleteAllTasksDialog.value = false
                        CoroutineScope(Dispatchers.IO).launch{
                            viewModel.deleteAllTasks()
                            notViewModel?.deleteAllNotifications()

                            withContext(Dispatchers.Main){
                                Toast.makeText(context, "Tüm Görevler ve bildirimler silindi", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }
                    ) {
                        Text("Sil", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSecondary)
                    }
                },
                dismissButton = {
                    Button(onClick = {ConfirmDeleteAllTasksDialog.value = false}) {
                        Text("Vazgeç",color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                            MaterialTheme.colorScheme.onSecondary)
                    }
                }
            )
        }



        SettingSection(title = "Görevler") {
            SettingItem(title = "Tüm görevler", value = "Göster"){

                showTasksDia = true

            }
            SettingItem(title = "Tüm Görevleri sil", value = "Sil", ActionTextColor = Color.Red){

                ConfirmDeleteAllTasksDialog.value = true

            }
        }
        Divider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp))

        // Yedekleme
        SettingSection(title = "Yedekleme") {
            SettingItem(title = "Verileri Dışa Aktar", value = "Dışa Aktar", ActionTextColor = Color.Blue){
                BackupUtils().exportAppDataAsZip(context)
            }
            SettingItem(title = "Verileri İçe Aktar", value = "İçe Aktar", ActionTextColor = Color.Cyan){
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "application/zip"
                }
                (context as Activity).startActivityForResult(intent, REQUEST_CODE_PICK_ZIP)


            }

        }
        Divider(Modifier.padding(horizontal = 8.dp, vertical = 16.dp))

        Spacer(Modifier.height(16.dp))

        // Diğer
        SettingSection(title = "Diğer") {
            SettingItem(title = "Hakkında", value = "Görüntüle"){
                val intent = Intent(context, AboutActivity::class.java)
                context.startActivity(intent)
            }
            SettingItem(title = "Geri Bildirim Gönder", value = "Gönder"){
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:tkaynar198@gmail.com".toUri()
                    putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim")
                    putExtra(Intent.EXTRA_TEXT, "Merhaba, uygulamanızla ilgili...")
                }

                if (emailIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(emailIntent)
                }else{
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("tkaynar198@gmail.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Geri Bildirim")
                        putExtra(Intent.EXTRA_TEXT, "Merhaba, uygulamanızla ilgili...")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "E-posta uygulaması seçin"))

                }

            }
            SettingItem(title = "Gizlilik Politikası", value = "Görüntüle"){
                val intent = Intent(context, PrivacyPolicyActivity::class.java)
                context.startActivity(intent)

            }
        }
    }
}


@Composable
fun ShowAllTasksDialog(taskViewModel: TaskViewModel, navController: NavController, onDismiss: ()->Unit) {
    val tasks by taskViewModel.paginatedTasks.collectAsState(initial = emptyList())
    val currentPage by taskViewModel.currentPage.collectAsState(initial = 1)

    var size by remember { mutableStateOf(0) }

    // Dialog açılınca görevleri yükle
    LaunchedEffect(Unit) {
        taskViewModel.loadTasks()
        size = taskViewModel.getTaskCount()
    }

    var showDia by remember{ mutableStateOf(true) }

    if(showDia){
        AlertDialog(
            containerColor = if(isLightTheme()) MaterialTheme.colorScheme.tertiaryContainer else  Color.DarkGray

            ,onDismissRequest = {
                onDismiss.invoke()
                showDia = false
            },
            title = { Text("Tüm Görevler ($size)", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                MaterialTheme.colorScheme.onSecondary)},
            text = {
                Column {
                    // Görevleri sayfalı bir şekilde listeleme
                    LazyColumn {
                        items(tasks) { task ->
                            Text(modifier = Modifier.fillMaxWidth().padding(16.dp).clickable {
                                navController.navigate("task_details/${task.id}")
                                onDismiss.invoke()
                                showDia = false
                            }, text = "${task.id}: ${task.title}", fontSize = 25.sp, color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onSecondary)
                        }
                    }

                    // Sayfa değiştirici
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        Button(
                            onClick = { taskViewModel.previousPage() },
                            enabled = currentPage > 0,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Önceki Sayfa", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onSecondary)
                        }

                        Button(
                            onClick = { taskViewModel.nextPage() },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Sonraki Sayfa", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                                MaterialTheme.colorScheme.onSecondary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onDismiss.invoke()
                    showDia = false
                }) {
                    Text("Kapat", color = if(isLightTheme()) MaterialTheme.colorScheme.primary else
                        MaterialTheme.colorScheme.onSecondary)
                }
            }
        )
    }

}




@Composable
fun SettingSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, top = 10.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 25.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        )
        Spacer(modifier = Modifier.height(20.dp))
        content()
    }
}

@Composable
fun SettingItem(title: String, value: String, TextColor: Color = MaterialTheme.colorScheme.onSecondary
                , ActionTextColor: Color =MaterialTheme.colorScheme.onSecondary.copy(if(isDarkTheme()) 0.7f else 1f) , click: ()->Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                click.invoke()
            }
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                color = TextColor
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 16.sp,
                color = ActionTextColor
            )
        )

    }

}

@Composable
fun SettingSwitchItem(title: String, isChecked: Boolean, checkedChange : (Boolean)->Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSecondary
            )
        )
        Switch(
            checked = isChecked,
            onCheckedChange = {
                checkedChange.invoke(it)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = Color.Green, uncheckedThumbColor = Color.Gray)
        )
    }
}
