package com.example.fap

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.fap.databinding.ActivityLoginBinding
import com.google.android.material.textfield.TextInputEditText

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */

class Login : AppCompatActivity() {

    enum class REGISTER_STATE {
        REGISTERED,
        REGISTERING,
        CONFIRMING
    }

    private var registerStatus: REGISTER_STATE = REGISTER_STATE.REGISTERED
    private var tmpPass: String = ""
    private lateinit var binding: ActivityLoginBinding
    private lateinit var lblLoginStatus: TextView
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

        lblLoginStatus = binding.lblLoginStatus
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

        textLogin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) { /* Arrow on mobile || Enter on Desktop */
                tryLogin()
                true
            } else {
                false
            }
        }

        btnLogin.setOnClickListener {
            tryLogin()
        }

        textLogin.setText("")

       if (! checkRegistered()) {
           registerStatus = REGISTER_STATE.REGISTERING
           lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
           lblLoginStatus.text = getString(R.string.register_password)
       }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Fix Back Button in Toolbar
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        // Implement Back Button
        when (registerStatus) {
            REGISTER_STATE.CONFIRMING -> {
                textLogin.text!!.clear()
                registerStatus = REGISTER_STATE.REGISTERING
                lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20F)
                lblLoginStatus.text = getString(R.string.register_password)
            }
            else -> {
                finish()
            }
        }
    }

    private fun checkRegistered(): Boolean {
        return true // TODO check if database file exists?
    }

    private fun tryLogin() {
        Log.d("Login", "Password: " + textLogin.text!!)

        // Disallow 0 length Passwords
        if (textLogin.text.isNullOrEmpty()) {
            return
        }

        when (registerStatus) {
            REGISTER_STATE.REGISTERED -> {
                Log.d("Login", "REGISTERED")
                if (textLogin.text!!.toString() == "000") {  // TODO check if passwords matches database?
                    // TODO send Database to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    lblLoginStatus.text = ""
                } else {
                    lblLoginStatus.text = getString(R.string.wrong_password)
                }
            }
            REGISTER_STATE.REGISTERING -> {
                tmpPass = textLogin.text!!.toString()
                lblLoginStatus.text = getString(R.string.confirm_password)
                registerStatus = REGISTER_STATE.CONFIRMING
            }
            REGISTER_STATE.CONFIRMING -> {
                if (textLogin.text!!.toString() == tmpPass) {
                    // TODO create Database with Password?
                    // TODO send Database to MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finishAffinity()
                    lblLoginStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14F)
                    lblLoginStatus.text = ""
                    registerStatus = REGISTER_STATE.REGISTERED
                } else {
                    lblLoginStatus.text = getString(R.string.retry_register_password)
                    registerStatus = REGISTER_STATE.REGISTERING
                }
            }
        }
        textLogin.text!!.clear()
    }
}