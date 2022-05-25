package dev.maharsh.todoapp.ui.tasks

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maharsh.todoapp.data.*
import dev.maharsh.todoapp.ui.ADD_TASK_RESULT_OK
import dev.maharsh.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val preferenceManager: PreferenceManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val searchQuery = savedStateHandle.getLiveData("searchQuery", "")

    val preferenceFlow = preferenceManager.preferenceFlow

    private val taskEventChannel = Channel<TaskEvent>()

    val taskEvent = taskEventChannel.receiveAsFlow()


    private val taskFlow =
        combine(
            searchQuery.asFlow(),
            preferenceFlow
        ) { query, filterPreferences: FilterPreferences ->
            Pair(query, filterPreferences)
        }.flatMapLatest { (query, filterPreferences) ->
            taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
        }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClicked(hideCompleted: Boolean) = viewModelScope.launch {
        preferenceManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }

    fun onTaskChecked(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked))
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClicked(task: Task) = viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClicked() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToAddTaskScreen)

    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task edited")
        }
    }

    private fun showTaskSavedConfirmationMessage(s: String) = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(s))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        taskEventChannel.send(TaskEvent.NavigateToDeleteAllCompletedScreen)
    }

    val tasks = taskFlow.asLiveData()

    sealed class TaskEvent {
        object NavigateToAddTaskScreen : TaskEvent()
        object NavigateToDeleteAllCompletedScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task) : TaskEvent()
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TaskEvent()
    }
}

