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

        AnalyticsTracker.setPreferredLanguage(this, getSelectedLanguage())
        navController.addOnDestinationChangedListener { _, destination, _ ->
            screenNameForDestination(destination.id)?.let { screenName ->
                AnalyticsTracker.logScreenView(this, screenName, "MainActivity")
            }
        }

        findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
        findViewById<android.widget.ImageButton>(R.id.btnSearch).setOnClickListener { }

        findViewById<android.view.View>(R.id.fabCreate).setOnClickListener {
            AnalyticsTracker.logTourCreateClick(this, "fab")
            Toast.makeText(this, "투어 제작 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        val drawer = findViewById<LinearLayout>(R.id.drawerMenu)

        fun navigateAndClose(destinationId: Int) {
            navController.navigate(destinationId)
            drawerLayout.closeDrawers()
        }

        drawer.findViewById<TextView>(R.id.menuNotice).setOnClickListener {
            AnalyticsTracker.logDrawerMenuSelect(this, "notice")
            navigateAndClose(R.id.noticeFragment)
        }
        drawer.findViewById<TextView>(R.id.menuLanguage).setOnClickListener {
            AnalyticsTracker.logDrawerMenuSelect(this, "language")
            navigateAndClose(R.id.languageFragment)
        }
        drawer.findViewById<TextView>(R.id.menuSettings).setOnClickListener {
            AnalyticsTracker.logDrawerMenuSelect(this, "settings")
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

    private fun getSelectedLanguage(): String {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "ko") ?: "ko"
    }

    private fun screenNameForDestination(destinationId: Int): String? {
        return when (destinationId) {
            R.id.homeFragment -> "home"
            R.id.artworkDetailFragment -> "artwork_detail"
            R.id.noticeFragment -> "notice"
            R.id.languageFragment -> "language"
            R.id.settingsFragment -> "settings"
            else -> null
        }
    }

    companion object {
        private const val PREFS = "artbusan_prefs"
        private const val KEY_LANGUAGE = "selected_language"
    }
}
