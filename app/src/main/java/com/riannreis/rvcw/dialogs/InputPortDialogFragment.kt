package com.riannreis.rvcw.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.riannreis.rvcw.R

class InputPortDialogFragment : DialogFragment() {

    private lateinit var portDialogListener: PortDialogListener

    interface PortDialogListener {
        fun onPortEntered(port: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            portDialogListener = context as PortDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context should implement PortDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = AlertDialog.Builder(it)
            // Get the layout inflater.
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog.
            // Pass null as the parent view because it's going in the dialog
            // layout.
            val view = inflater.inflate(R.layout.dialog_choose_port, null)
            // Add action buttons.

            val okBtn: Button = view.findViewById(R.id.btn_ok)
            val cancelBtn: Button = view.findViewById(R.id.btn_cancel)
            val portInput: EditText = view.findViewById(R.id.port_input)

            cancelBtn.setOnClickListener {
                dialog?.cancel()
            }

            okBtn.setOnClickListener {
                val portText = portInput.text.toString()
                if (portText.isNotEmpty()) {
                    val port = portText.toInt()
                    if (port in 1024..65535) {
                        Toast.makeText(requireContext(), "Valid port: $port", Toast.LENGTH_SHORT)
                            .show()
                        portDialogListener.onPortEntered(port)
                        dismiss() // Closes dialog.
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Invalid port! Value must be between 1024 and 65535.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please, insert a value.", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            // removed because it was duplicating the "cancel" and "OK" buttons

//                .setPositiveButton(android.R.string.ok,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        // Sign in the user.
//                    })
//                .setNegativeButton(android.R.string.cancel,
//                    DialogInterface.OnClickListener { dialog, id ->
//                        getDialog()?.cancel()
//                    })

            builder.setView(view)

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}