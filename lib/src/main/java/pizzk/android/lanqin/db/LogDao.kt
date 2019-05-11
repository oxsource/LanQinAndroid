package pizzk.android.lanqin.db

import androidx.room.*

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(log: LogTextEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(logs: List<LogTextEntity>): List<Int>

    @Query("SELECT * FROM `${LogTextEntity.TABLE}` WHERE id = :id")
    fun query(id: Int): LogTextEntity?

    @Query("SELECT * FROM `${LogTextEntity.TABLE}` WHERE sync in (:syncs) LIMIT :size OFFSET :page*:size")
    fun query(page: Int, size: Int, syncs: Array<Int>): List<LogTextEntity>

    @Update
    fun update(log: LogTextEntity): Int

    @Update
    fun updateAll(logs: List<LogTextEntity>): Int

    @Delete
    fun delete(log: LogTextEntity): Int

    @Delete
    fun deleteAll(logs: List<LogTextEntity>): Int
}