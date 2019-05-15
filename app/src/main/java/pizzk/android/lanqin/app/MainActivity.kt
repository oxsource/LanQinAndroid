package pizzk.android.lanqin.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.LanQin
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {
    private lateinit var tvContent: TextView
    private lateinit var bt: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvContent = findViewById(R.id.tvContent)
        bt = findViewById(R.id.bt)
        bt.setOnClickListener {
            doAsync {
                val entity = LanQinEntity()
                try {
                    throw IllegalArgumentException("测试校验参数不正确")
                } catch (e: Exception) {
                    entity.errStack = LanQinEntity.stackTrace(e)
                }
                entity.errTag = "This is Android Test001"
                LanQin.upload(entity)
                val logs = LanQin.logs(0, 5)
                uiThread {
                    val content = logs.joinToString("\n") { it.content }
                    tvContent.text = content
                }
            }
        }
        //加载
        doAsync {
            val logs = LanQin.logs(0, 5)
            uiThread {
                val content = logs.joinToString("\n") { it.content }
                tvContent.text = content
            }
        }
    }
}
