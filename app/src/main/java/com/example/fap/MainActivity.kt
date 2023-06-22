package com.example.fap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.example.fap.data.FapDatabase
import com.example.fap.data.Wallet
import com.example.fap.databinding.ActivityMainBinding
import com.example.fap.ui.dialogs.AddPayment
import com.example.fap.ui.login.Login
import com.example.fap.utils.SharedPreferencesManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        binding.appBarMain.fab.setOnClickListener {
            startActivity(Intent(this, AddPayment::class.java))
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    startActivity(Intent(this, Login::class.java))
                    finishAffinity()
                    true
                }
                else -> {
                    navController.navigate(menuItem.itemId)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return when (item.itemId) {
            R.id.action_settings -> {
                if (navController.currentDestination?.id != R.id.nav_settings) {
                    navController.navigate(R.id.nav_settings)
                }
                true
            }
            R.id.action_add_wallet -> {
                // create add wallet dialog
                val dialogBuilder = AlertDialog.Builder(this, R.style.Theme_FAP)
                val dialogLayout =  LayoutInflater.from(this).inflate(R.layout.dialog_add_wallet, null)
                val input: TextInputEditText = dialogLayout.findViewById(R.id.add_wallet_input)

                dialogBuilder.setTitle("Add a new Wallet")

                dialogBuilder.setView(dialogLayout)

                dialogBuilder.setPositiveButton("Save") { dialog, _ ->
                    if (input.text.isNullOrEmpty()) {
                        Snackbar.make(
                            binding.root,
                            "Please fill field",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        lifecycleScope.launch {
                            val db = FapDatabase.getInstance(applicationContext).fapDao()
                            val curUser = SharedPreferencesManager.getInstance(applicationContext)
                                .getCurUser(applicationContext)
                            db.insertWallet(Wallet(input.text.toString(), curUser))
                        }
                        dialog.dismiss()
                    }
                }
                dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = dialogBuilder.create()
                dialog.show()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
