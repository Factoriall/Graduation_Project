package org.techtown.graduateproject

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val startButton : Button = findViewById(R.id.startButton)
        startButton.setOnClickListener{
            
        }
    }
}