package org.techtown.graduateproject

import androidx.room.*

@Dao
interface DayRecordDao {
    @Query("SELECT * FROM DayRecord")
    suspend fun getAll(): List<DayRecord>

    @Query("SELECT * FROM DayRecord WHERE recordId LIKE :recordId")
    suspend fun findById(recordId: Int): DayRecord

    @Query("SELECT * FROM DayRecord WHERE dayOfMonth LIKE :day AND month LIKE :month AND year LIKE :year")
    suspend fun findByDate(year: Int, month: Int, day: Int): DayRecord

    @Delete
    suspend fun delete(record: DayRecord)

    @Insert
    suspend fun insert(record: DayRecord)

    @Update
    suspend fun update(record: DayRecord)
}