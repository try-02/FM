package com.crouton.alpha.android

import android.app.Activity
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import com.crouton.alpha.presentation.DaftarBerkasLayar
import com.crouton.alpha.presentation.StateLayarBerkas

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tampilan = ComposeView(this)
        tampilan.setContent { DaftarBerkasLayar(StateLayarBerkas(), {}) }
        setContentView(tampilan)
    }
}
