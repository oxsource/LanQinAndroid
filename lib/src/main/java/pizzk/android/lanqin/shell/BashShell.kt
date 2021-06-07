package pizzk.android.lanqin.shell

import pizzk.android.lanqin.utils.Logger
import java.util.*
import java.util.concurrent.locks.ReentrantLock


/**
 * Shell脚本
 */
abstract class BashShell {
    companion object {
        //shell池
        private val pools: MutableList<BashShell> = LinkedList()
        private val using: MutableList<BashShell> = LinkedList()

        //计数器
        private var count: Int = 0

        @Synchronized
        @JvmStatic
        fun <T : BashShell> obtain(clazz: Class<T>): BashShell {
            val shellNil: BashShell? = pools.find { it.javaClass == clazz }
            if (null != shellNil) {
                pools.remove(shellNil)
                ++count
            }
            val shell: BashShell = shellNil ?: clazz.newInstance()
            shell.seal(value = false)
            using.add(shell)
            shell.execute(cmd = "echo")
            return shell
        }

        @Synchronized
        @JvmStatic
        fun recycle(shell: BashShell) {
            if (using.contains(shell)) using.remove(shell)
            if (pools.contains(shell)) return
            shell.seal(value = true)
            pools.add(shell)
        }

        @Synchronized
        @JvmStatic
        fun release() {
            using.forEach { shell -> shell.seal(value = true) }
            using.clear()
            pools.forEach { shell -> shell.seal(value = true) }
            pools.clear()
        }

        @JvmStatic
        fun <T : BashShell, R> with(clazz: Class<T>, fallback: R, block: (BashShell) -> R): R {
            val shell: BashShell = obtain(clazz)
            return try {
                block(shell)
            } catch (e: Exception) {
                fallback
            } finally {
                recycle(shell)
            }
        }
    }

    //实例名称
    protected val name: String = "${this.javaClass.simpleName}@$count"

    //同步锁
    protected val lock: ReentrantLock = ReentrantLock()

    //密闭指令(回收时锁定后无法连接使用，必需通过obtain方法重新获取)
    @Volatile
    protected var seal: Boolean = false

    /**密闭指令*/
    private fun seal(value: Boolean) {
        this.seal = value
        if (value) disconnect()
    }

    protected fun catcher(block: () -> Unit, info: String) {
        //@formatter:off
        try {
            block()
        } catch (e: Exception) {
            Logger.e("$name $info: ${e.message}")
        }
        //@formatter:on
    }

    protected abstract fun connect(): Boolean

    protected abstract fun disconnect()

    protected abstract fun read(buf: StringBuffer): Boolean

    protected abstract fun write(cmd: String): Boolean

    open fun support(): Boolean = true

    abstract fun execute(cmd: String, retry: Boolean = true): String

    abstract fun successful(result: String): Boolean
}