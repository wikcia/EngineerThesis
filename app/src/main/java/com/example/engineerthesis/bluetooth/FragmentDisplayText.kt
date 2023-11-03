package com.example.engineerthesis.bluetooth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.engineerthesis.R

class FragmentDisplayText : Fragment() {
    private var textSendListener: TextSendListener? = null

    /* This code allows a fragment to communicate with an activity,
     if this activity implements the appropriate interface.
     Otherwise, it raises an exception to point out the absence of a suitable implementation. */
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is TextSendListener) {
            textSendListener = context
        } else {
            throw RuntimeException("$context must implement TextSendListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display_text, container, false)
        val sendTextView = view.findViewById<EditText>(R.id.messageEditText)
        val sendButton = view.findViewById<Button>(R.id.sendTextButton)


        sendButton.setOnClickListener {
            if (BluetoothManager.getBluetoothConnected()) {
                val textToSend = sendTextView.text.toString()
                textSendListener?.onTextSend(textToSend)
            } else {
                Toast.makeText(context, "No Bluetooth connection to the robot", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
