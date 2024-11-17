package org.techtown.testlogfilter

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.techtown.testlogfilter.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val loading: Dialog by lazy { loadingDialog() }

    companion object {
        private val TAG: String = MainActivity::class.java.simpleName
        const val REQUEST_CODE_PERMISSIONS = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        checkAndRequestPermissions(this)
        initView()
    }

    override fun onResume() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onResume()
    }

    override fun onPause() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onPause()
    }

    override fun onStart() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onStart()
    }

    override fun onStop() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onStop()
    }

    override fun onDestroy() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onDestroy()
    }

    private fun initView() = binding.apply {
        logButton.setOnClickListener {
            loading.show()

            it.isEnabled = false
            statusTextView.text = "검색 중"
            getLog()
        }

    }

    private fun checkAndRequestPermissions(activity: Activity) {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        // Android 11 이상 - MANAGE_EXTERNAL_STORAGE 권한 확인
        if (!Environment.isExternalStorageManager()) {
            val intent =
                Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + activity.packageName))
            activity.startActivity(intent)
        }
    }

    // 결과 확인을 위해 Activity의 onRequestPermissionsResult에 추가
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
                getLog()
            } else {
                // 권한이 거부됨
                toast("파일 읽기 및 쓰기 권한이 필요합니다.")
            }
        }
    }

    private fun getLog() {
        val method = Thread.currentThread().stackTrace[2].methodName
        debug(TAG, "$method called")
        // 코루틴 시작
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                repeat(20) {
                    // IO 스레드에서 파일 작업
                    val inputFilePath =
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/server-log",
                            "log.txt"
                        )
                    val outputFilePath =
                        File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/server-log",
                            "filtered_log.txt"
                        )

                    if (inputFilePath.exists()) {
                        val lines = inputFilePath.readLines()
                        val filteredLines = mutableListOf<String>()

                        for (i in 1 until lines.size) {
                            if (lines[i].startsWith("[Elevator->Platform]")) {
                                filteredLines.add(lines[i - 1]) // 윗줄 추가
                                filteredLines.add(lines[i]) // 현재 줄 추가
                            }
                        }

                        // 필터링된 결과를 출력 파일에 작성
                        outputFilePath.bufferedWriter().use { writer ->
                            filteredLines.forEach { line ->
                                writer.write(line)
                                writer.newLine()
                            }
                        }

                        debug(TAG, "Filtered log saved to: ${outputFilePath.absolutePath}")
                    } else {
                        withContext(Dispatchers.Main) {
                            error(TAG, "log.txt 파일을 찾을 수 없습니다.")
                            toast("log.txt 파일을 찾을 수 없습니다.")
                        }
                        return@withContext
                    }

                }
            }

            // UI 업데이트 (Main 스레드)
            binding.logButton.isEnabled = true
            binding.statusTextView.text = "작업 완료"
            toast("Filtered log saved successfully!")
            loading.cancel()
        }
    }
}