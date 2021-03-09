package com.example.picchat.ui.main

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.picchat.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeletePostDialog: DialogFragment() {

    private var positiveListener: (() -> Unit)? = null

    fun setPositiveListener(listener: () -> Unit) {
        positiveListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setIcon(R.drawable.delete_post_img)
                .setPositiveButton("Yes"){ _, _ ->
                    positiveListener?.let {
                        it()
                    }
                }
                .setNegativeButton("Cancel") { dialogInterface, _ ->
                    dialogInterface.cancel()
                }
                .create()
    }
}