package dev.maharsh.todoapp.ui.deleteallcompleted

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment: DialogFragment() {

    private val viewModel : DeleteAllCompletedViewModel by viewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Do you really want to delte all completed tasks?")
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Yes"){ _,_->
                viewModel.onConfirmClick()
            }
            .create()
}