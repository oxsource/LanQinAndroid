package pizzk.android.lanqin.db

import android.app.Application
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

@Database(entities = [LogTextEntity::class], version = 1, exportSchema = false)
internal abstract class LanQinDatabase : RoomDatabase() {

    abstract fun log(): LogDao

    companion object {
        private const val DB_NAME = "LanQin-DB"
        fun create(app: Application): LanQinDatabase {
            val clazz: Class<LanQinDatabase> = LanQinDatabase::class.java
            return Room.databaseBuilder(app, clazz, DB_NAME).build()
        }

        //https://github.com/android/architecture-components-samples/issues/330
        fun truncate(context: Context, vararg tables: String) {
            var instance: SQLiteDatabase? = null
            val block: (SQLiteDatabase, String) -> Unit = { db: SQLiteDatabase, table: String ->
                try {
                    //清除数据
                    val deleteSql = "DELETE FROM '$table';"
                    db.execSQL(deleteSql)
                    //重置自增索引
                    val resetSql = "DELETE FROM sqlite_sequence WHERE name = '$table';"
                    db.execSQL(resetSql)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            try {
                val file: File = context.getDatabasePath(DB_NAME)
                val db: SQLiteDatabase = SQLiteDatabase.openOrCreateDatabase(file, null)
                instance = db
                tables.iterator().forEach { table: String -> block(db, table) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                instance?.close()
            }
        }
    }
}