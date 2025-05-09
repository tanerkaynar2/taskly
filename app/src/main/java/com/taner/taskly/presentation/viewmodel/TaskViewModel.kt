package com.taner.taskly.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taner.taskly.core.utils.DateUtils.Companion.daysOfWeek
import com.taner.taskly.core.utils.DateUtils.Companion.shouldResetTask
import com.taner.taskly.core.utils.DateUtils.Companion.timeFormat
import com.taner.taskly.data.repository.TaskRepository
import com.taner.taskly.data.local.entity.TaskEntity
import com.taner.taskly.data.local.mapper.toDomain
import com.taner.taskly.data.local.mapper.toEntity
import com.taner.taskly.domain.model.Task
import com.taner.taskly.domain.model.TaskCategory
import com.taner.taskly.domain.model.TaskRepetition
import com.taner.taskly.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Date

class TaskViewModel(private val taskRepository: TaskRepository) : ViewModel() {

    var selectedFiles = mutableStateOf<List<Uri>>(emptyList())

    fun setSelectedFiles(uris: List<Uri>) {
        selectedFiles.value = uris
    }

    suspend fun deleteAllTasks() {
        taskRepository.deleteAllTasks()
    }


    private val _paginatedTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val paginatedTasks: StateFlow<List<TaskEntity>> = _paginatedTasks
    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage
    private val _pageSize = MutableStateFlow(10)
    val pageSize: StateFlow<Int> = _pageSize
    suspend fun setPageTasksPaginated(page:Int,pageSize:Int = 10){
        _currentPage.value = page
        _pageSize.value = pageSize
    }

    fun loadTasks() {
        viewModelScope.launch {
            val offset = (_currentPage.value - 1) * _pageSize.value
            val loadedTasks = taskRepository.getTasksPaginated(_pageSize.value, offset)
            _paginatedTasks.value = loadedTasks
        }
    }

    fun nextPage() {
        viewModelScope.launch {
            _currentPage.value++
            loadTasks()
        }
    }

    fun previousPage() {
        viewModelScope.launch {
            if (_currentPage.value > 1) {
                _currentPage.value--
                loadTasks()
            }
        }
    }



    fun addSelectedFiles(uris: List<Uri>) {
        selectedFiles.value = selectedFiles.value.toMutableList().apply {
            addAll(uris)
        }
    }
    fun removeSelectedFiles(uris: List<Uri>) {
        selectedFiles.value = selectedFiles.value.toMutableList().apply {
            removeAll(uris)
        }
    }

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    var tasks: StateFlow<List<Task>> = _tasks
    private val _todayTasks = MutableStateFlow<List<Task>>(emptyList())
    var todayTasks: StateFlow<List<Task>> = _todayTasks
    private val _tasksByDate = MutableStateFlow<List<Task>>(emptyList())
    var tasksByDate: StateFlow<List<Task>> = _tasksByDate



    // Günlük görevleri almak
    fun getDailyTasks(){
        viewModelScope.launch {
            _tasks.value = taskRepository.getTasksByCategory(TaskCategory.DAILY).map { it.toDomain() }
        }
    }


    /*
    if(it.date!=null){
                val date1 = dateFormat.format(Date(it.date))
                val date2 = dateFormat.format(Date(today))
                date1 == date2
            }else false
     */

    fun getTodayTasks(){



        val calendar = Calendar.getInstance()
        val dayOfMonthIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1  // 10 ise -> 9
        val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7


        val calendar2 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar2.timeInMillis
        calendar2.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = start + 24 * 60 * 60 * 1000 - 1

        viewModelScope.launch {
            _todayTasks.value =  taskRepository.getTodayTasks(start,endOfDay,daysOfWeek.get(dayOfWeekIndex),dayOfMonthIndex).map { it.toDomain() }
        }
        /*
        val sortedTasks = tasks.value.filter {
            it.status != TaskStatus.COMPLETED
        }.sortedWith(compareBy(nullsLast()) { task ->
            task.time?.let { timeFormat.parse(it) }
        })*/
    }

    fun getTasksByDate(date: Long, whereFilter: ((TaskEntity) -> Boolean)? = null
                       , orderBy: ((TaskEntity) -> Comparable<Any>)? = null
                       , upcomingFilter: (Boolean)? = null
    ,locationFilter: String?=null){

        val calendar = Calendar.getInstance().apply {
            timeInMillis = date
        }
        val dayOfMonthIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1  // 10 ise -> 9
        val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7


        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = date
        }
        calendar2.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar2.timeInMillis
        calendar2.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = start + 24 * 60 * 60 * 1000 - 1


        val selectedMonthDayIndex = (Calendar.getInstance().apply {
            timeInMillis = date
        }.get(Calendar.DAY_OF_MONTH) - 1)

        viewModelScope.launch {
            var list =  taskRepository.getTasksByDate(start,endOfDay,daysOfWeek.get(dayOfWeekIndex),dayOfMonthIndex)

            list = list.filter {
                if(listOf(0,1,2).contains(selectedMonthDayIndex)){
                    if(it.repetition == TaskRepetition.MONTHLY){
                        (it.days?.split("||")?.contains("${selectedMonthDayIndex}") == true)
                    }else true
                }else true
            }


            whereFilter?.let {
                list = list.filter(it)
            }
            upcomingFilter?.let {b->
                list = list.filter{
                    if(it.time!=null){

                        val taskTime = timeFormat.parse(it .time)
                        val now = timeFormat.parse(timeFormat.format(Date()))

                        taskTime != null && (if(b){
                            taskTime.after(now) && !it.isCompleted
                        }else now.after(taskTime))


                    }else true
                }
            }

            orderBy?.let {
                list = list.sortedBy(it)
            }

            locationFilter?.let{f->
                list = list.filter { it.locations?.contains(f)==true }
            }

            _tasksByDate.value = list.map { it.toDomain() }


        }
        /*
        val sortedTasks = tasks.value.filter {
            it.status != TaskStatus.COMPLETED
        }.sortedWith(compareBy(nullsLast()) { task ->
            task.time?.let { timeFormat.parse(it) }
        })*/
    }

    init {
        // Veri yüklendikten sonra bir kere çağırabilirsiniz
        viewModelScope.launch {
           // getDailyTasks() // İlk veri çekme işlemi
            //getTodayTasks()
        }
    }

    // Haftalık görevleri almak
    suspend fun getWeeklyTasks() = taskRepository.getTasksByCategory(TaskCategory.WEEKLY)

    // Özel görevleri almak
    suspend fun getSpecialTasks() = taskRepository.getTasksByCategory(TaskCategory.CUSTOM)

    suspend fun getTaskById(id: Int) = taskRepository.getTaskById(id)

    suspend fun getLastTaskId():Int?{
        return taskRepository.getLastTask()?.id
    }

    suspend fun runAnalysis(context: Context){
        taskRepository.runAnalysis(context)
    }

    suspend fun getLastTask():Task?{
        return taskRepository.getLastTask()?.toDomain()
    }

    // Görev ekleme
    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
           //val currentTasks = _tasks.value ?: emptyList()
           //_tasks.value = currentTasks + task.toDomain()
            taskRepository.addTask(task)
            getTodayTasks()
        }
    }

    // Görev güncelleme
    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.updateTask(task)
        }
    }

    fun delTask(task: TaskEntity) {
        viewModelScope.launch {
            taskRepository.delTask(task)
        }
    }

    suspend fun deleteTasksOlderThanTwoDays(twoDaysAgo: Long){
        taskRepository.deleteTasksOlderThanTwoDays(twoDaysAgo)
    }
    suspend fun getTaskCount():Int{
        return taskRepository.getTaskCount()
    }

    // Tamamlanan görev sayısını almak
    suspend fun getCompletedTaskCount() = taskRepository.getCompletedTaskCount()
    fun syncForToday() {



        val calendar = Calendar.getInstance().apply {
            timeInMillis = Date().time
        }
        val dayOfMonthIndex = calendar.get(Calendar.DAY_OF_MONTH) - 1  // 10 ise -> 9
        val dayOfWeekIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7


        val calendar2 = Calendar.getInstance().apply {
            timeInMillis = Date().time
        }
        calendar2.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar2.timeInMillis
        calendar2.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = start + 24 * 60 * 60 * 1000 - 1

        viewModelScope.launch {
            taskRepository.getTasksByDate(start,endOfDay,daysOfWeek.get(dayOfWeekIndex),dayOfMonthIndex).map { it.toDomain() }.forEach {

                if(shouldResetTask(it)){
                    if(it.isCompleted || it.status == TaskStatus.COMPLETED){
                        updateTask(it.copy(isCompleted=false,status = TaskStatus.NOT_STARTED).toEntity())

                    }
                }

            }
        }

    }
}
