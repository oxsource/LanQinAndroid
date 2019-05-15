package pizzk.android.lanqin.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LogTextEntity::class], version = 1, exportSchema = false)
internal abstract class LanQinDatabase : RoomDatabase() {

    abstract fun log(): LogDao

    companion object {
        fun create(app: Application): LanQinDatabase {
            return Room.databaseBuilder(
                app,
                LanQinDatabase::class.java, "LanQin-DB"
            ).build()
        }
    }
}

internal fun RoomDatabase.truncate(vararg tables: String) {
    try {
        val ssd: SupportSQLiteDatabase = this.openHelper.writableDatabase
        tables.iterator().forEach { table ->
            //清除数据
            val deleteSql = "DELETE FROM $table;"
            ssd.execSQL(deleteSql)
            //重置自增索引
            val resetSql = "UPDATE sqlite_sequence SET seq = 0 WHERE name = '$table';"
            ssd.execSQL(resetSql)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}