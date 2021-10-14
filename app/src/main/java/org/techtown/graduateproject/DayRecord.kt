package org.techtown.graduateproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "DayRecord")
data class DayRecord (
        @PrimaryKey(autoGenerate = true) val recordId: Int,
        val year : Int,
        val month : Int,
        val dayOfMonth : Int,
        var perfect : Int,
        var bad : Int,
        var time : Long){
        constructor(year: Int, month: Int, dayOfMonth: Int, perfect: Int, bad: Int, time:Long)
                : this(0, year, month, dayOfMonth, perfect, bad, time)
}