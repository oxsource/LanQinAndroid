package pizzk.android.lanqin.db

import androidx.room.*

@Dao
internal interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: LogTextEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(logs: List<LogTextEntity>): List<Long>

    @Query("SELECT * FROM `log-text` WHERE id = :id")
    fun query(id: Int): LogTextEntity?

    @Query("SELECT * FROM `log-text` LIMIT :size OFFSET :page*:size")
    fun queryAll(page: Int, size: Int): List<LogTextEntity>

    @Delete
    fun delete(log: LogTextEntity): Int

    @Delete
    fun deleteAll(logs: List<LogTextEntity>): Int
}