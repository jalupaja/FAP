package com.example.fap

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.ImageButton
import com.example.fap.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var textLogin: TextInputEditText
    private lateinit var btnLogin: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up the user interaction to manually show or hide the system UI.
        textLogin = binding.passwordInput
        btnLogin = binding.btnLogin

        textLogin.setText("")

        // TODO textLogin onEnter...
        // TODO single line login text

        textLogin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) { /* Arrow on mobile || Enter on Desktop */
                checkPassword()
                true
            } else {
                false
            }
        }

        btnLogin.setOnClickListener {
            checkPassword()
        }
    }

    private fun checkPassword() {
        Log.d("Login", "Password: " + textLogin.text!!)

        if (textLogin.text!!.toString() == "000") {  // TODO implement password logic
            val intent = Intent(this, MainActivity::class.java)
            /* use following code, if back button returns to login */
            //val stackBuilder = TaskStackBuilder.create(this)
            //stackBuilder.addNextIntentWithParentStack(intent)
            //stackBuilder.startActivities()
            startActivity(intent)
            finishAffinity()
            textLogin.text!!.clear()
        } else {
            textLogin.text!!.clear()
            // TODO add error label
        }
    }
}