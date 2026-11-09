package com.crouton.alpha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.crouton.alpha.data.android.RepositoriBerkasAndroid
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.usecase.DaftarIsiUseCase
import com.crouton.alpha.presentation.DaftarBerkasLayar
import com.crouton.alpha.presentation.PenyimpanStateBerkas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositori = RepositoriBerkasAndroid(filesDir)
        val penyimpan = PenyimpanStateBerkas(DaftarIsiUseCase(repositori), scope)
        val dasar = filesDir.absolutePath
        penyimpan.muatAwal(LokasiBerkas(dasar, "Penyimpanan internal", dasar, true))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                penyimpan.kembali()
            }
        })

        setContent {
            val state by penyimpan.state.collectAsState()
            MaterialTheme {
                DaftarBerkasLayar(state) { item ->
                    penyimpan.bukaItem(item.pengenal, item.nama, item.uriAtauPath, item.adalahFolder)
                }
            }
        }
    }
}
