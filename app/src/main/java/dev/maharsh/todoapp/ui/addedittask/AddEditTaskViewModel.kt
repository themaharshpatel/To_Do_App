package dev.maharsh.todoapp.ui.addedittask


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maharsh.todoapp.data.Task
import dev.maharsh.todoapp.data.TaskDao
import dev.maharsh.todoapp.ui.ADD_TASK_RESULT_OK
import dev.maharsh.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    fun onSaveClick() {
        if (taskName.isBlank()) {
            showInvalidInputMessage("Name cannot be empty")
            return
        }
        if (task != null) {
            val updatedTask = task.copy(name = taskName, important = taskImportance)
            updateTask(updatedTask)
        } else {
            val newTask = Task(name = taskName, important = taskImportance)
            createTask(newTask)
        }
    }

    private fun showInvalidInputMessage(s: String)  = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(s))
    }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    private fun createTask(newTask: Task) = viewModelScope.launch {
        taskDao.insert(newTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(updatedTask: Task) = viewModelScope.launch {
        taskDao.update(updatedTask)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    val task = savedStateHandle.get<Task>("task")

    var taskName = savedStateHandle.get<String>("taskName") ?: task?.name ?: ""
        set(value) {
            field = value
            savedStateHandle.set("taskName", value)
        }
    var taskImportance = savedStateHandle.get<Boolean>("taskImportance") ?: task?.important ?: false
        set(value) {
            field = value
            savedStateHandle.set("taskImportance", value)
        }

    sealed class AddEditTaskEvent{
        data class ShowInvalidInputMessage(val msg:String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result:Int) : AddEditTaskEvent()
    }

}