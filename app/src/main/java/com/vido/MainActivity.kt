package com.vido

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import android.view.KeyEvent
import android.view.View
import com.vido.ui.home.HomeFragment
import kotlinx.android.synthetic.main.fragment_home.view.*


class MainActivity : AppCompatActivity() {
    private lateinit  var navView: BottomNavigationView
    public lateinit  var homeFragment: HomeFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (navView.selectedItemId == R.id.navigation_home) {
                if (!(homeFragment!!.backPressed())) {
                    super.onKeyDown(keyCode, event)
                } else false
            } else super.onKeyDown(keyCode, event)

        } else super.onKeyDown(keyCode, event)
    }

}
