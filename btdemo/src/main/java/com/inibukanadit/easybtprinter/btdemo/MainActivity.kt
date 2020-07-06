package com.inibukanadit.easybtprinter.btdemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inibukanadit.easybtprinter.ui.BTPrinterActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_open_discoveries.setOnClickListener {
            startActivity(Intent(this, BTPrinterActivity::class.java).apply {
                putExtra(Intent.EXTRA_TITLE, getString(R.string.blutooth_manager))
            })
        }
    }
}
