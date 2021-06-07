package pizzk.android.lanqin

import android.app.Application
import android.os.Looper
import android.widget.Toast
import org.jetbrains.anko.doAsync
import pizzk.android.lanqin.db.LanQinDatabase
import pizzk.android.lanqin.db.LogTextEntity
import pizzk.android.lanqin.entity.LanQinEntity
import pizzk.android.lanqin.utils.JsonUtils
import pizzk.android.lanqin.repos.CloudRepos
import pizzk.android.lanqin.repos.LocalRepos
import pizzk.android.lanqin.utils.HashUtils
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.os.Process
import pizzk.android.lanqin.shell.AdbShell
import pizzk.android.lanqin.utils.IoUtils
import pizzk.android.lanqin.utils.Logger
import java.io.BufferedInputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess

/**
 * 蓝芩APP性能及BUG采集SDK
 */
object LanQin {
    data class Config(
        val appId: String,
        val channel: String,
        /**上送服务器地址，不合规的地址将不进行上送*/
        val host: String,
        /**最大缓存时间*/
        val expireDays: Int,
        var debug: Boolean = true
    ) {
        constructor(channel: String, debug: Boolean) : this(
            appId = "",
            channel = channel,
            host = "",
            expireDays = 1,
            debug = debug
        )
    }

    private lateinit var config: Config
    private lateinit var app: Application

    /**
     * 初始化蓝芩SDK
     */
    fun init(context: Application, cfg: Config) {
        app = context
        config = cfg
    }

    fun config() = config

    fun app() = app

    /**使用数据库进行操作*/
    private fun <T> database(then: (LanQinDatabase) -> T): T? {
        var instance: LanQinDatabase? = null
        return try {
            val db: LanQinDatabase = LanQinDatabase.create(app())
            instance = db
            then(db)
        } catch (e: Exception) {
            null
        } finally {
            instance?.close()
        }
    }

    /**
     * 存储及上送日志
     */
    fun upload(vararg entity: LanQinEntity): Boolean {
        val es: List<LanQinEntity> = entity.toList()
        val logs: List<LogTextEntity> = try {
            es.map { e: LanQinEntity ->
                val log = LogTextEntity()
                val content: String = JsonUtils.json(e)
                e.hash = HashUtils.sha(content)
                log.content = JsonUtils.json(e)
                log.time = e.happenTime
                return@map log
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        if (config().host.isEmpty()) {
            Logger.shadow(es)
            return true
        }
        val cached: Boolean = database { LocalRepos.save(logs, it) > 0 } ?: false
        if (CloudRepos.save(es) <= 0 || !cached) return false
        database { it.log().deleteAll(logs) }
        return true
    }

    /**
     * 上送未同步的日志
     */
    fun uploads() {
        val logs: List<LogTextEntity> = logs(page = 0, size = 5) ?: return
        if (logs.isEmpty()) return
        //有效与无效日志分组
        val now: Long = Date().time
        val expire: Int = config.expireDays * 24 * 60 * 60
        val invalid: (LogTextEntity) -> Boolean = { e: LogTextEntity -> now - e.time >= expire }
        val group: Map<Boolean, List<LogTextEntity>> = logs.groupBy(invalid)
        //移除无效日志
        val bad: List<LogTextEntity> = group[true] ?: emptyList()
        if (bad.isNotEmpty()) database { it.log().deleteAll(bad) }
        //上传有效日志
        if (config().host.isEmpty()) return
        val good: List<LogTextEntity> = group[false] ?: emptyList()
        val es: List<LanQinEntity> = good.mapNotNull { JsonUtils.parse<LanQinEntity>(it.content) }
        if (CloudRepos.save(es) <= 0) return
        database { it.log().deleteAll(good) }
    }

    /**
     * 查询本地未上送日志
     */
    fun logs(page: Int, size: Int): List<LogTextEntity>? {
        if (page < 0 || size < 0) return null
        return database { it.log().queryAll(page, size) }
    }

    /**
     * 清空本地所有日志
     */
    fun cleanLogs() {
        LanQinDatabase.truncate(app(), LogTextEntity.TABLE)
        Logger.truncate()
    }

    //异步任务
    fun asyncTask(block: () -> Unit) {
        doAsync { block() }
    }

    //包装配置LanQin CrashHandler
    fun withCrashHandler(seconds: Long = 5) {
        val handler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val exp: Throwable = throwable ?: Exception("UncaughtException")
            val latch = CountDownLatch(1)
            doAsync {
                upload(LanQinEntity.throwable(exp))
                latch.countDown()
            }
            Thread {
                Looper.prepare()
                Toast.makeText(app, R.string.lan_qin_app_crash_hint, Toast.LENGTH_LONG).show()
                Looper.loop()
            }.start()
            latch.await(seconds, TimeUnit.SECONDS)
            handler?.uncaughtException(thread, exp)
            Process.killProcess(Process.myPid())
            exitProcess(status = -1)
        }
    }

    //清除logcat日志
    fun cleanLogcat() {
        AdbShell.nimble("logcat -c")
    }

    //自定义收集logcat日志操作
    fun dumpLogcat(block: (File) -> Unit) {
        val logSuffix = ".log"
        val file: File = kotlin.runCatching {
            val context = app().applicationContext
            val cacheDir: File = context.externalCacheDir ?: context.cacheDir
            val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.CHINA)
            //dump log to file
            val time = sdf.format(Date())
            val features = StringBuffer()
            features.append(android.os.Build.MODEL.replace("\\s".toRegex(), ""))
            features.append("-")
            features.append(android.os.Build.VERSION.SDK_INT)
            val file = File(cacheDir, "LanQin_${time}_$features${logSuffix}")
            AdbShell.nimble("logcat -df ${file.absolutePath}")
            if (!file.exists()) return@runCatching null
            if (file.length() > 0) return@runCatching file
            file.delete()
            return@runCatching null
        }.getOrNull() ?: return Logger.e("dump logcat file is not exist or length is 0.")
        //log file zip
        val zip: File? = kotlin.runCatching {
            val path = "${file.absolutePath.replace(logSuffix, "")}.zip"
            val zipFile = File(path)
            ZipOutputStream(zipFile.outputStream()).use { outs ->
                val entry = ZipEntry(file.name)
                val ins: BufferedInputStream = file.inputStream().buffered()
                outs.putNextEntry(entry)
                IoUtils.write(ins, outs)
                outs.closeEntry()
            }
            return@runCatching zipFile
        }.getOrNull()
        file.delete()
        zip ?: return
        //consume action
        kotlin.runCatching { block(zip) }
        if (zip.exists()) zip.delete()
    }

    //收集logcat日志并上报到服务端
    fun dumpLogcat() {
        dumpLogcat { CloudRepos.save(it) }
    }
}