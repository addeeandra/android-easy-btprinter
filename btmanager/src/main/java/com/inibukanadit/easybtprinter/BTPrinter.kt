package com.inibukanadit.easybtprinter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.inibukanadit.easybtprinter.ui.dialog.BTPrinterDialogFragment

object BTPrinter {

    fun print(activity: AppCompatActivity, content: String) {
        BTPrinterDialogFragment()
            .apply { arguments = Bundle().apply { putString(BTPrinterDialogFragment.KEY_CONTENT, content) } }
            .show(activity.supportFragmentManager, null)
    }

}