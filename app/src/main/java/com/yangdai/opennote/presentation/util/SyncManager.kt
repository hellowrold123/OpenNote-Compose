package com.yangdai.opennote.presentation.util

import android.content.Context
import android.util.Log
import com.yangdai.opennote.data.local.entity.DavData
import com.yangdai.opennote.presentation.state.WebDavConfigState
import com.yangdai.opennote.presentation.util.Constants.WebDavConfigInfo.WEBDAV_FILE_DIRNAME
import com.yangdai.opennote.presentation.util.Constants.WebDavConfigInfo.WEBDAV_FILE_SUFFIX
import com.yangdai.opennote.presentation.viewmodel.SharedViewModel
import com.thegrizzlylabs.sardineandroid.DavResource
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import com.thegrizzlylabs.sardineandroid.impl.SardineException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class SyncManager(
    val webDavConfigState: WebDavConfigState
) {
    private suspend fun getSardine(): OkHttpSardine {
        val sardine = OkHttpSardine()
        sardine.setCredentials(webDavConfigState.webDavUsername, webDavConfigState.webDavPassword, true)
        return sardine
    }

    private fun ensureDirectoryExists(sardine: OkHttpSardine, dirUrl: String) {
        if (!sardine.exists(dirUrl)) {
            sardine.createDirectory(dirUrl)
        }
    }
    private fun getLastTimeFile(davDatas: List<DavData>): DavData? {
        val firstOrNull = davDatas.filter { item -> item.name.endsWith(WEBDAV_FILE_SUFFIX) }
            .sortedByDescending { it.modified }.firstOrNull()
        return firstOrNull
    }
    suspend fun uploadFile(fileName: String?, fileDir: String, localFile: File?): String = withContext(Dispatchers.IO) {

        try {
            val sardine = getSardine()
            val serverUrl = webDavConfigState.webDavUrl
            ensureDirectoryExists(sardine, serverUrl + fileDir)
            val url = "$serverUrl$fileDir/$fileName"
            if (sardine.exists(url)) {
                sardine.delete(url)
            }
            sardine.put(url, localFile, "application/x-www-form-urlencoded")
            "Success：$fileDir/$fileName"
        } catch (e: IOException) {
            e.printStackTrace()
            e.message.toString()
        }
    }
    suspend fun uploadToWebDAV(
        fileName: String,
        fileDir: String,
        data: ByteArray,
        mimeType: String = "text/plain"
    ): Result<String> =  withContext(Dispatchers.IO) {
        try {
            val sardine = getSardine()
            val serverUrl = webDavConfigState.webDavUrl
            ensureDirectoryExists(sardine, serverUrl + fileDir)
            val url = "$serverUrl$fileDir/$fileName"
            if (sardine.exists(url)) {
                sardine.delete(url)
            }
            sardine.put(url, data, mimeType)
            Result.success("$fileDir/$fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkConnection(url: String, account: String, pwd: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val sardine = OkHttpSardine()
        sardine.setCredentials(account, pwd, true)
        return@withContext try {
            sardine.exists(url)
//            sardine.createDirectory(url+"BNOTE1q")
//            Pair(true, context.getString(R.string.webdav_config_success))
//            Pair(true,"success")
            Result.success(true)
        } catch (e: Exception) {
//            e.printStackTrace()

            Result.failure(e)
        }
    }


    suspend fun downloadFileByPath(webPath: String, localDir: String): String? = withContext(Dispatchers.IO) {
        try {

            val davServerUrl =webDavConfigState.webDavUrl
            val sardine = getSardine()
            val fileName = webPath.substringAfterLast("/")
            val localPath = File(localDir, fileName).path
            Log.i("wutao", "downloadFileByPath: $davServerUrl$webPath")
            sardine.get(davServerUrl + webPath).use { inputStream ->
                FileOutputStream(localPath).use { outputStream ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        outputStream.flush()
                    }
                }
            }
            localPath
        } catch (e: Exception) {
            Log.i("SyncManage::error",e.message.toString())
            e.printStackTrace()
            null
        }
    }

    suspend fun downloadFileLastTime(davDatas:List<DavData>): Result<InputStream> = withContext(Dispatchers.IO) {
        try {

            val davServerUrl =webDavConfigState.webDavUrl
            val sardine = getSardine()

            val lastData=getLastTimeFile(davDatas)
            var name=lastData?.name
            val fileName = WEBDAV_FILE_DIRNAME+"/"+name
//            val localPath = File(localDir, fileName).path
            Log.i("wutao", "downloadFileByPath: $fileName")

            Result.success(sardine.get(davServerUrl + fileName))
        } catch (e: Exception) {
            Log.i("SyncManage::error",e.message.toString())
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun listAllFile(dir: String?): Result<List<DavData>> = withContext(Dispatchers.IO) {

        try {
            val davServerUrl =webDavConfigState.webDavUrl
            val sardine = getSardine()
            val resources = sardine.list(davServerUrl + dir) //如果是目录一定别忘记在后面加上一个斜杠
            val davData: MutableList<DavData> = ArrayList()
            for (i: DavResource in resources) {
                davData.add(DavData(i))
            }
            Result.success(davData)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("SyncManage::error",e.message.toString())
            Result.failure(e)
        }
    }

}