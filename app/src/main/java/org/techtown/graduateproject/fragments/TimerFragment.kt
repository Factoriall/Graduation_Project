package org.techtown.graduateproject.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import kotlinx.coroutines.runBlocking
import org.techtown.graduateproject.AppDatabase
import org.techtown.graduateproject.DayRecord
import org.techtown.graduateproject.R
import org.techtown.graduateproject.activities.CameraActivity
import java.util.*

class TimerFragment: Fragment() {
    private var todayTime : Long = 0
    private var perfectCnt : Int = 0
    private var badCnt : Int = 0
    private lateinit var timer : TextView
    private lateinit var perfectValue : TextView
    private lateinit var badValue : TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_timer, container, false)
        val db = AppDatabase.getInstance(requireContext())!!
        val cal = Calendar.getInstance()

        perfectValue = view.findViewById(R.id.perfectValue)
        badValue = view.findViewById(R.id.badValue)
        timer = view.findViewById(R.id.todayTimer)
        cal.time = Date()

        var todayRecord: DayRecord?
        runBlocking {
            todayRecord = db.dayRecordDao().findByDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
        if(todayRecord != null) {
            todayTime = todayRecord!!.time
            perfectCnt = todayRecord!!.perfect
            badCnt = todayRecord!!.bad
        }
        perfectValue.text = perfectCnt.toString()
        badValue.text = badCnt.toString()
        timer.text = getTimeString(todayTime)


        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data!!
                todayTime = data.getLongExtra("time", 0)
                perfectCnt = data.getIntExtra("perfect", 0)
                badCnt = data.getIntExtra("bad", 0)

                timer.text = getTimeString(todayTime)
                perfectValue.text = perfectCnt.toString()
                badValue.text = badCnt.toString()
            }
        }

        val startButton : Button = view.findViewById(R.id.startButton)
        startButton.setOnClickListener{
            val intent = Intent(requireContext(), CameraActivity::class.java)
            resultLauncher.launch(intent)
        }

        return view
    }

    private fun getTimeString(time : Long) : String {
        return String.format("%02d:%02d:%02d",
            (time / 3600000), ((time % 3600000) / 60000), ((time % 60000) / 1000))
    }
}