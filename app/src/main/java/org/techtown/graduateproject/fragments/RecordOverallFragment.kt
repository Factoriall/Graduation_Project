package org.techtown.graduateproject.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlinx.coroutines.runBlocking
import org.techtown.graduateproject.AppDatabase
import org.techtown.graduateproject.DayRecord
import org.techtown.graduateproject.R

class RecordOverallFragment: Fragment() {
    private lateinit var recordList : List<DayRecord>
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_record_overall, container, false)

        val db = AppDatabase.getInstance(requireContext())!!
        runBlocking {
            recordList = db.dayRecordDao().getAll()
        }
        val totalNum : TextView = view.findViewById(R.id.totalNumValue)
        val totalTime : TextView = view.findViewById(R.id.totalTimeValue)
        val totalDay : TextView = view.findViewById(R.id.totalDayValue)
        val perfectPercent : TextView = view.findViewById(R.id.perfectPercentValue)

        var perfect = 0
        var bad = 0
        var time : Long = 0
        var day = 0
        for(record in recordList){
            day += 1
            time += record.time
            bad += record.bad
            perfect += record.perfect
        }

        totalNum.text = (perfect + bad).toString() + "회"
        totalTime.text = getTimeString(time)
        totalDay.text = day.toString() + "일"
        perfectPercent.text = (perfect * 100 / (perfect + bad)).toString() + "%"

        return view
    }

    private fun getTimeString(time : Long) : String {
        return String.format("%02d:%02d:%02d",
            (time / 3600000), ((time % 3600000) / 60000), ((time % 60000) / 1000))
    }
}