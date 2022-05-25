package dev.maharsh.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.maharsh.todoapp.R
import dev.maharsh.todoapp.data.SortOrder
import dev.maharsh.todoapp.data.Task
import dev.maharsh.todoapp.databinding.FragmentTasksBinding
import dev.maharsh.todoapp.util.exhaustive
import dev.maharsh.todoapp.util.onQueryTextChanged
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_tasks) {

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var searchView: SearchView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)


        val tasksAdapter = TasksAdapter(
            object : TasksAdapter.OnItemClickListener {
                override fun onItemClicked(task: Task) {
                    viewModel.onTaskSelected(task)
                }

                override fun onItemChecked(task: Task, isChecked: Boolean) {
                    viewModel.onTaskChecked(task, isChecked)
                }

            }
        )


        binding.apply {
            recyclerViewTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    if (position != RecyclerView.NO_POSITION)
                        viewModel.onTaskSwiped(tasksAdapter.currentList[viewHolder.adapterPosition])
                }

            }
            ).attachToRecyclerView(recyclerViewTasks)

            newItemButton.setOnClickListener {
                viewModel.onAddNewTaskClicked()
            }

        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            tasksAdapter.submitList(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.taskEvent.collect { event ->
                when (event) {
                    is TaskViewModel.TaskEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task Deleted", LENGTH_SHORT)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClicked(event.task)
                            }.show()
                    }
                    is TaskViewModel.TaskEvent.NavigateToAddTaskScreen -> {
                        val action =
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(
                                "Add Task",
                                null
                            )
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.NavigateToEditTaskScreen -> {
                        val action =
                            TaskFragmentDirections.actionTaskFragmentToAddEditTaskFragment(
                                "Edit Task",
                                event.task
                            )
                        findNavController().navigate(action)
                    }
                    is TaskViewModel.TaskEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, LENGTH_SHORT).show()
                    }
                    TaskViewModel.TaskEvent.NavigateToDeleteAllCompletedScreen -> {
                        val action =
                            TaskFragmentDirections.actionGlobalDeleteAllCompletedDialogFragment()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
        setFragmentResultListener("add_edit_result") { _, resultBundle ->
            val result = resultBundle.getInt("add_edit_result")
            viewModel.onAddEditResult(result)
        }

        setHasOptionsMenu(true)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value

        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.action_hide_completed_tabs).isChecked =
                viewModel.preferenceFlow.first().hideCompleted
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }

            R.id.action_sort_by_date -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }

            R.id.action_hide_completed_tabs -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClicked(item.isChecked)
                true
            }
            R.id.action_all_completed_task -> {
                viewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}