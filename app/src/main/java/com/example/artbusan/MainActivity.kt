package com.example.artbusan

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("artbusan_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("selected_language", "ko") ?: "ko"
        super.attachBaseContext(LocaleHelper.wrap(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        drawerLayout = findViewById(R.id.drawerLayout)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
        findViewById<android.widget.ImageButton>(R.id.btnSearch).setOnClickListener { }

        findViewById<android.view.View>(R.id.fabCreate).setOnClickListener {
            Toast.makeText(this, "투어 제작 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        val drawer = findViewById<LinearLayout>(R.id.drawerMenu)

        fun navigateAndClose(destinationId: Int) {
            navController.navigate(destinationId)
            drawerLayout.closeDrawers()
        }

        drawer.findViewById<TextView>(R.id.menuNotice).setOnClickListener {
            navigateAndClose(R.id.noticeFragment)
        }
        drawer.findViewById<TextView>(R.id.menuLanguage).setOnClickListener {
            navigateAndClose(R.id.languageFragment)
        }
        drawer.findViewById<TextView>(R.id.menuSettings).setOnClickListener {
            navigateAndClose(R.id.settingsFragment)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
