package com.aoihosizora.desktoptips

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick
import com.tbruyelle.rxpermissions2.RxPermissions

class MainActivity : AppCompatActivity(), IContextHelper {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        requestPermission {
            initUI()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun initUI() {
        showToast("TODO")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_select_tab -> onMenuSelectTabClicked()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onMenuSelectTabClicked() {
        showToast("TODO")
    }

    @OnClick(R.id.btn_test)
    fun onBtnTestClicked() {
        showToast("TODO")
    }

    private fun requestPermission(callback: () -> Unit) {
        val requiredPermissions = listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (requiredPermissions.isEmpty()) {
            callback()
            return
        }

        showAlert(
            title = "权限", message = "本应用需要读取本地内存，请授予权限。", cancelable = false,
            posText = "确定", posListener = { _, _ ->
                RxPermissions(this).request(*requiredPermissions).subscribe {
                    if (it) {
                        showToast("授权成功。")
                        callback()
                    } else {
                        showAlert(
                            title = "授权", message = "授予权限失败，请重新启动程序进行授权。",
                            posText = "退出", posListener = { _, _ -> finish() }
                        )
                    }
                }
            }, negText = "取消", negListener = { _, _ -> finish() }
        )
    }
}
