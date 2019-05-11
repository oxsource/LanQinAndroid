package pizzk.android.lanqin.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LogTextEntity::class], version = 1)
abstract class LanQinDatabase : RoomDatabase() {

    abstract fun log(): LogDao

    companion object {
        private const val DB_NAME: String = "LanQin-DB"

        fun create(app: Application): LanQinDatabase {
            return Room.databaseBuilder(
                app,
                LanQinDatabase::class.java, DB_NAME
            ).build()
        }
    }
}