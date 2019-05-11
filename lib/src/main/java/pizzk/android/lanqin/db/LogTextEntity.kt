package pizzk.android.lanqin.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = LogTextEntity.TABLE)
data class LogTextEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    /**是否已经与云端同步，未同步-0，已同步-1*/
    @ColumnInfo(name = "sync")
    var sync: Int = 0,
    /**日志内容*/
    @ColumnInfo(name = "content")
    var content: String = "",
    /**发生时间*/
    @ColumnInfo(name = "time")
    var time: Long = 0
) {

    fun hasSync(): Boolean = sync == SYNCED_CODE

    fun setSynced(code: Int) {
        sync = code
    }

    companion object {
        const val TABLE = "log-text"
        const val NOT_SYNC_CODE: Int = 0
        const val SYNCED_CODE: Int = 1
    }
}