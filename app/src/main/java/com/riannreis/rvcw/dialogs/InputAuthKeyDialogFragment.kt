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

class InputAuthKeyDialogFragment : DialogFragment() {

    private lateinit var authDialogListener: AuthDialogListener

    interface AuthDialogListener {
        fun onAuthKeyEntered(secretKey: String)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            authDialogListener = context as AuthDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context should implement AuthDialogListener")
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val builder = AlertDialog.Builder(it)

            val inflater = requireActivity().layoutInflater

            val view = inflater.inflate(R.layout.dialog_basic_auth, null)

            val okBtn: Button = view.findViewById(R.id.btn_ok)
            val cancelBtn: Button = view.findViewById(R.id.btn_cancel)
            val authKey: EditText = view.findViewById(R.id.auth_input)

            cancelBtn.setOnClickListener {
                dialog?.cancel()
            }

            okBtn.setOnClickListener {
                val secretKey = authKey.text.toString()
                if (secretKey.isNotEmpty()) {
                    if (secretKey.length > 20) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.value_too_long),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.valid_value, secretKey),
                            Toast.LENGTH_SHORT
                        ).show()

                        authDialogListener.onAuthKeyEntered(secretKey)
                        dismiss()

                        val sharedPrefs =
                            requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPrefs.edit()

                        editor.putString("authKey", secretKey)
                        editor.apply()

                    }

                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.insert_value), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            builder.setView(view)

            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}