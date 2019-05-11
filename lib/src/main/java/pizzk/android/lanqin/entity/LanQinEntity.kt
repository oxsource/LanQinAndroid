package pizzk.android.lanqin.entity

import android.app.Application
import pizzk.android.lanqin.main.LanQin
import java.util.*

/**
 * 信息实体
 */
data class LanQinEntity(
    /**
     * 常规信息
     * appId-应用ID
     * appVersion-应用版本号
     * appChannel-应用渠道
     * happenTime-记录发生时间
     * phoneModel-手机型号
     * osInfo-系统信息
     * sdkInt-SDK版本
     */
    var appId: String = "",
    var appVersion: String = "",
    var appChannel: String = "",
    var happenTime: Long = 0,
    var phoneModel: String = "",
    var osInfo: String = "",
    var sdkInt: Int = 0,

    /**
     * 内存信息:
     * max-最大内存
     * total-已申请内存
     * free-可用内存
     * usage-使用内存
     */
    var memMax: Long = 0,
    var memTotal: Long = 0,
    var memFree: Long = 0,
    var memUsage: Long = 0,
    /**记录网络请求耗时(单位：描述)*/
    var netSpendSec: Int = 0,

    /**
     * 错误信息
     * errTag-错误标签
     * errStack-错误堆栈
     * errExtra-错误补充信息
     */
    var errTag: String = "",
    var errStack: String = "",
    var errExtra: String = "",

    /**
     * 用户信息
     * userAccount-用户账户信息
     * lastLoginTime-最近一次登录时间
     */
    var userAccount: String = "",
    var lastLoginTime: String = "",
    /**日志级别：普通-0，调试-1，错误-2，奔溃-3*/
    var level: Int = LEVEL_NORMAL
) {

    init {
        val app: Application = LanQin.app()
        val now = Date()
        /**获取基础信息*/
        appId = LanQin.config().appId
        appVersion = app.packageManager.getPackageInfo(app.packageName, 0).versionName
        appChannel = LanQin.config().channel
        happenTime = now.time
        phoneModel = android.os.Build.MODEL
        osInfo = android.os.Build.VERSION.RELEASE
        sdkInt = android.os.Build.VERSION.SDK_INT
        /**内存信息*/
        val runtime: Runtime = Runtime.getRuntime()
        memMax = runtime.maxMemory()
        memTotal = runtime.totalMemory()
        memFree = runtime.freeMemory()
        memUsage = runtime.totalMemory() - runtime.freeMemory()
    }

    companion object {
        /**日志级别*/
        const val LEVEL_NORMAL = 0
        const val LEVEL_DEBUG = 1
        const val LEVEL_ERROR = 2
        const val LEVEL_CRASH = 3
    }
}