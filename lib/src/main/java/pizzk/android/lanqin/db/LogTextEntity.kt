package pizzk.android.lanqin.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = LogTextEntity.TABLE)
data class LogTextEntity(
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    /**日志内容*/
    @ColumnInfo(name = "content")
    var content: String = "",
    /**发生时间*/
    @ColumnInfo(name = "time")
    var time: Long = 0
) {
    companion object {
        const val TABLE = "log-text"
    }
}