package com.crouton.alpha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.crouton.alpha.presentation.DaftarBerkasLayar
import com.crouton.alpha.presentation.StateLayarBerkas

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DaftarBerkasLayar(StateLayarBerkas(), {})
        }
    }
}
