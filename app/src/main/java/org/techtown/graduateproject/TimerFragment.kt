package org.techtown.graduateproject

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlin.math.abs

class TimerFragment: Fragment() {
    private lateinit var timer : TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        timer = view.findViewById(R.id.todayTimer)

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){

                val time = result.data!!.getLongExtra("time", 0)
                Log.d("timerFragment", time.toString())

                timer.text = (time / 60000).toString() + ":" + (time/1000).toString() + ":" + ((time % 1000) / 10).toString()
            }
        }
        val startButton : Button = view.findViewById(R.id.startButton)
        startButton.setOnClickListener{
            val intent = Intent(requireContext(), CameraActivity::class.java)
            resultLauncher.launch(intent)
        }

        return view
    }
}