package com.example.artbusan

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.activity.enableEdgeToEdge

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var drawerLayout: DrawerLayout

    // Bottom nav views
    private lateinit var navHome: LinearLayout
    private lateinit var navExplore: LinearLayout
    private lateinit var navCreate: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navigation setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        drawerLayout = findViewById(R.id.drawerLayout)

        // Bottom nav
        navHome = findViewById(R.id.navHome)
        navExplore = findViewById(R.id.navExplore)
        navCreate = findViewById(R.id.navCreate)
        navProfile = findViewById(R.id.navProfile)

        navHome.setOnClickListener { navigateTo(R.id.homeFragment) }
        navExplore.setOnClickListener { navigateTo(R.id.exploreFragment) }
        navCreate.setOnClickListener { navigateTo(R.id.createFragment) }
        navProfile.setOnClickListener { navigateTo(R.id.profileFragment) }

        // Top bar buttons
        findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
        findViewById<android.widget.ImageButton>(R.id.btnSearch).setOnClickListener {
            navigateTo(R.id.exploreFragment)
        }

        // Drawer menu items
        val drawer = findViewById<LinearLayout>(R.id.drawerMenu)
        drawer.findViewById<TextView>(R.id.menuNotice).setOnClickListener {
            drawerLayout.closeDrawers()
        }
        drawer.findViewById<TextView>(R.id.menuGuide).setOnClickListener {
            drawerLayout.closeDrawers()
        }
        drawer.findViewById<TextView>(R.id.menuLanguage).setOnClickListener {
            drawerLayout.closeDrawers()
        }
        drawer.findViewById<TextView>(R.id.menuAccessibility).setOnClickListener {
            drawerLayout.closeDrawers()
        }
        drawer.findViewById<TextView>(R.id.menuSettings).setOnClickListener {
            drawerLayout.closeDrawers()
        }

        // Listen for destination changes to update bottom nav state
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateBottomNavState(destination.id)
            updateTopBarTitle(destination.id)
        }
    }

    private fun navigateTo(destinationId: Int) {
        if (navController.currentDestination?.id == destinationId) return
        navController.navigate(destinationId)
    }

    private fun updateBottomNavState(destinationId: Int) {
        // Reset all to unselected
        setTabUnselected(navExplore, R.id.navExploreIcon, R.id.navExploreLabel)
        setTabUnselected(navCreate, R.id.navCreateIcon, R.id.navCreateLabel)
        setTabUnselected(navProfile, R.id.navProfileIcon, R.id.navProfileLabel)
        setHomeUnselected()

        when (destinationId) {
            R.id.homeFragment -> setHomeSelected()
            R.id.exploreFragment -> setTabSelected(navExplore, R.id.navExploreIcon, R.id.navExploreLabel)
            R.id.createFragment -> setTabSelected(navCreate, R.id.navCreateIcon, R.id.navCreateLabel)
            R.id.profileFragment -> setTabSelected(navProfile, R.id.navProfileIcon, R.id.navProfileLabel)
        }
    }

    private fun setHomeSelected() {
        setTabSelected(navHome, R.id.navHomeIcon, R.id.navHomeLabel)
    }

    private fun setHomeUnselected() {
        setTabUnselected(navHome, R.id.navHomeIcon, R.id.navHomeLabel)
    }

    private fun setTabSelected(tab: LinearLayout, iconId: Int, labelId: Int) {
        tab.findViewById<ImageView>(iconId)
            .setColorFilter(getColor(R.color.primary_teal))
        tab.findViewById<TextView>(labelId)
            .setTextColor(getColor(R.color.primary_teal))
    }

    private fun setTabUnselected(tab: LinearLayout, iconId: Int, labelId: Int) {
        tab.findViewById<ImageView>(iconId)
            .setColorFilter(getColor(R.color.text_secondary))
        tab.findViewById<TextView>(labelId)
            .setTextColor(getColor(R.color.text_secondary))
    }

    private fun updateTopBarTitle(destinationId: Int) {
        val title = when (destinationId) {
            R.id.homeFragment -> getString(R.string.museum_name)
            R.id.exploreFragment -> getString(R.string.nav_explore)
            R.id.createFragment -> getString(R.string.nav_create)
            R.id.profileFragment -> getString(R.string.nav_profile)
            else -> getString(R.string.museum_name)
        }
        findViewById<TextView>(R.id.tvTopTitle).text = title
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
