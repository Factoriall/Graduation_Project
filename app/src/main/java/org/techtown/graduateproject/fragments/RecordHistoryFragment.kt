package org.techtown.graduateproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import org.techtown.graduateproject.R

class RecordHistoryFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recrod_history, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        return view
    }
}