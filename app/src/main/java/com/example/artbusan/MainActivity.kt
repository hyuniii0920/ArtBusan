package com.example.artbusan

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
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

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

        // 상단 바 버튼
        findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
        findViewById<android.widget.ImageButton>(R.id.btnSearch).setOnClickListener {
            // 검색창으로 포커스 (HomeFragment의 etSearch)
        }

        // FAB — 투어 제작
        findViewById<FloatingActionButton>(R.id.fabCreate).setOnClickListener {
            Toast.makeText(this, "투어 제작 (준비 중)", Toast.LENGTH_SHORT).show()
        }

        // 드로어 메뉴 항목
        val drawer = findViewById<LinearLayout>(R.id.drawerMenu)
        listOf(
            drawer.findViewById<TextView>(R.id.menuNotice),
            drawer.findViewById(R.id.menuGuide),
            drawer.findViewById(R.id.menuLanguage),
            drawer.findViewById(R.id.menuAccessibility),
            drawer.findViewById(R.id.menuSettings)
        ).forEach { it.setOnClickListener { drawerLayout.closeDrawers() } }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
