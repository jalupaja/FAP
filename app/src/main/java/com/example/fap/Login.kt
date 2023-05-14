package com.example.fap

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
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
    private lateinit var btn1: Button
    private lateinit var btn2: Button
    private lateinit var btn3: Button
    private lateinit var btn4: Button
    private lateinit var btn5: Button
    private lateinit var btn6: Button
    private lateinit var btn7: Button
    private lateinit var btn8: Button
    private lateinit var btn9: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btn0: Button
    private lateinit var btnLogin: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        textLogin = binding.passwordInput
        btnLogin = binding.btnLoginAccept
        btn1 = binding.btnLogin1
        btn2 = binding.btnLogin2
        btn3 = binding.btnLogin3
        btn4 = binding.btnLogin4
        btn5 = binding.btnLogin5
        btn6 = binding.btnLogin6
        btn7 = binding.btnLogin7
        btn8 = binding.btnLogin8
        btn9 = binding.btnLogin9
        btn0 = binding.btnLogin0
        btnBack = binding.btnLoginBack

        btn1.setOnClickListener {
            textLogin.append("1")
        }
        btn2.setOnClickListener {
            textLogin.append("2")
        }
        btn3.setOnClickListener {
            textLogin.append("3")
        }
        btn4.setOnClickListener {
            textLogin.append("4")
        }
        btn5.setOnClickListener {
            textLogin.append("5")
        }
        btn6.setOnClickListener {
            textLogin.append("6")
        }
        btn7.setOnClickListener {
            textLogin.append("7")
        }
        btn8.setOnClickListener {
            textLogin.append("8")
        }
        btn9.setOnClickListener {
            textLogin.append("9")
        }
        btn0.setOnClickListener {
            textLogin.append("0")
        }

        btnBack.setOnClickListener {
            val current = textLogin.text?.toString()
            if (!current.isNullOrEmpty()) {
                textLogin.setText(current?.substring(0, current.length - 1))
            }
        }

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


        textLogin.setText("")
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