package pizzk.android.lanqin.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import pizzk.android.lanqin.LanQin
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.Logger

class MainActivity : AppCompatActivity() {
    private lateinit var tvContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvContent = findViewById(R.id.tvContent)
        findViewById<View>(R.id.bt).setOnClickListener {
            try {
                throw IllegalArgumentException("test throw exception")
            } catch (e: Exception) {
                LanQin.asyncTask {
                    Logger.write("test log line.\n", true)
                    LanQin.upload(LanQinEntity.throwable(e))
                    runOnUiThread { loadLogs() }
                }
            }
        }
        findViewById<View>(R.id.btUnCatch).setOnClickListener {
            throw IllegalArgumentException("test unCatchException")
        }
        loadLogs()
    }

    private fun loadLogs() {
        doAsync {
            val logs: List<LogTextEntity> = LanQin.logs(0, 5) ?: return@doAsync
            uiThread {
                val content: String = logs.joinToString("\n") { it.content }
                tvContent.text = content
            }
        }
    }
}
