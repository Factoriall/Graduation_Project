package org.techtown.graduateproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.runBlocking
import org.techtown.graduateproject.AppDatabase
import org.techtown.graduateproject.DayRecord
import org.techtown.graduateproject.R
import org.techtown.graduateproject.RecordHistoryRecyclerAdapter

class RecordHistoryFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recrod_history, container, false)
        val db = AppDatabase.getInstance(view.context)!!
        val recordList : List<DayRecord>
        runBlocking {
            recordList = db.dayRecordDao().getAll()
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        val linearLayoutManager = LinearLayoutManager(view.context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = RecordHistoryRecyclerAdapter(recordList)


        return view
    }
}