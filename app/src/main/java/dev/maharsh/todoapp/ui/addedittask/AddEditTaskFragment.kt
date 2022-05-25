package dev.maharsh.todoapp.ui.addedittask

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.maharsh.todoapp.R
import dev.maharsh.todoapp.databinding.FragmentAddEditTaskBinding
import dev.maharsh.todoapp.util.exhaustive
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {

    private val viewModel: AddEditTaskViewModel by viewModels()


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            importantTask.isChecked = viewModel.taskImportance
            importantTask.jumpDrawablesToCurrentState()
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created ${viewModel.task?.createdDateFormatted}"

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            importantTask.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }

        }


        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.editTextTaskName.clearFocus()
                        setFragmentResult("add_edit_result",
                        bundleOf("add_edit_result" to event.result))
                        findNavController().popBackStack()
                    }
                    is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                    }
                }

            }
        }


    }
}