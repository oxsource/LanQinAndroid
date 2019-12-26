package pizzk.android.lanqin.db

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase

@Database(entities = [LogTextEntity::class], version = 1, exportSchema = false)
internal abstract class LanQinDatabase : RoomDatabase() {

    abstract fun log(): LogDao

    companion object {
        fun create(app: Application): LanQinDatabase {
            val name = "LanQin-DB"
            val clazz = LanQinDatabase::class.java
            return Room.databaseBuilder(app, clazz, name).build()
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