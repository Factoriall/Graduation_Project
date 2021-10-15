package org.techtown.graduateproject

import android.annotation.SuppressLint
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecordHistoryRecyclerAdapter(
    private val records: List<DayRecord>)
    : RecyclerView.Adapter<RecordHistoryRecyclerAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_record, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(records[position])
    }

    override fun getItemCount(): Int {
        return records.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val date = itemView.findViewById<TextView>(R.id.dateText)
        private val time = itemView.findViewById<TextView>(R.id.timeText)
        private val perfect = itemView.findViewById<TextView>(R.id.perfectValue)
        private val bad = itemView.findViewById<TextView>(R.id.badValue)
        private val percent = itemView.findViewById<TextView>(R.id.percentValue)

        @SuppressLint("SetTextI18n", "SimpleDateFormat")
        fun setItem(dayRecord: DayRecord) {
            val cal = Calendar.getInstance()
            cal.set(dayRecord.year, dayRecord.month, dayRecord.dayOfMonth)
            date.text = SimpleDateFormat("yyyy/MM/dd").format(cal.time);
            time.text = getTimeString(dayRecord.time)
            val p = dayRecord.perfect
            val b = dayRecord.bad
            perfect.text = p.toString()
            bad.text = b.toString()
            percent.text = (p * 100 / (p+b)).toString() + "%"
        }

        private fun getTimeString(time : Long) : String {
            return String.format("%02d:%02d:%02d",
                (time / 3600000), ((time % 3600000) / 60000), ((time % 60000) / 1000))
        }
    }
}