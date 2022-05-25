package dev.maharsh.todoapp.ui.deleteallcompleted;

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.maharsh.todoapp.data.TaskDao
import dev.maharsh.todoapp.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeleteAllCompletedViewModel @Inject constructor(
    private val taskDao: TaskDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel(){

    fun onConfirmClick() = applicationScope.launch {
        taskDao.deleteCompletedTasks()
    }

}
