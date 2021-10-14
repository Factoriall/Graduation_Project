package org.techtown.graduateproject.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.techtown.graduateproject.AppDatabase
import org.techtown.graduateproject.R
import org.techtown.graduateproject.fragments.RecordFragment
import org.techtown.graduateproject.fragments.TimerFragment

class MainActivity: AppCompatActivity() {
    private val timerFragment by lazy { TimerFragment() }
    private val recordFragment by lazy { RecordFragment() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNavigationBar()
    }

    private fun initNavigationBar() {
        val bnv = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bnv.setOnItemSelectedListener {
            when(it.itemId){
                R.id.timerTab ->{
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, timerFragment as Fragment)
                        .commit()

                }
                R.id.recordTab ->{
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, recordFragment as Fragment)
                        .commit()
                }
            }
            true
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, timerFragment as Fragment)
            .commit()

    }
}