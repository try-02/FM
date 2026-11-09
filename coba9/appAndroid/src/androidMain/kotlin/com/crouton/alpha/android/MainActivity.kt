package com.crouton.alpha.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.crouton.alpha.data.android.RepositoriBerkasAndroid
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.model.StatusSimpan
import com.crouton.alpha.domain.usecase.DaftarIsiUseCase
import com.crouton.alpha.domain.usecase.SimpanPerubahanUseCase
import com.crouton.alpha.presentation.DaftarBerkasLayar
import com.crouton.alpha.presentation.EditorLayar
import com.crouton.alpha.presentation.PenyimpanStateBerkas
import com.crouton.alpha.presentation.PenyimpanStateEditor
import com.crouton.alpha.presentation.StatusEditor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repositori = RepositoriBerkasAndroid(filesDir)
        val simpanUseCase = SimpanPerubahanUseCase(repositori)
        val penyimpan = PenyimpanStateBerkas(DaftarIsiUseCase(repositori), scope)
        val editorPenyimpan = PenyimpanStateEditor()
        buatDataPercontoh()
        val dasar = filesDir.absolutePath
        penyimpan.muatAwal(LokasiBerkas(dasar, "Penyimpanan internal", dasar, true))

        setContent {
            val state by penyimpan.state.collectAsState()
            val stateEditor by editorPenyimpan.state.collectAsState()
            var berkasDibuka by remember { mutableStateOf<LokasiBerkas?>(null) }
            var versiDibuka by remember { mutableStateOf(0L) }

            fun simpanEditor() {
                val berkas = berkasDibuka ?: return
                scope.launch {
                    val hasil = simpanUseCase.jalankan(berkas, KontenBerkas(stateEditor.teks), versiDibuka)
                    if (hasil.status == StatusSimpan.TERSIMPAN) {
                        editorPenyimpan.tandaiTersimpan()
                        editorPenyimpan.sembunyikanDialogTutup()
                        versiDibuka = repositori.versiEksternal(berkas)
                    } else {
                        editorPenyimpan.tandaiGagal(hasil.pesan)
                    }
                }
            }

            fun tutupEditor() {
                if (stateEditor.status == StatusEditor.BERUBAH) {
                    editorPenyimpan.tampilkanDialogTutup()
                } else {
                    berkasDibuka = null
                }
            }

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (berkasDibuka != null) {
                        tutupEditor()
                    } else {
                        penyimpan.kembali()
                    }
                }
            })

            MaterialTheme {
                if (berkasDibuka != null) {
                    EditorLayar(
                        state = stateEditor,
                        saatTeksBerubah = { editorPenyimpan.ubahTeks(it) },
                        saatSimpan = { simpanEditor() },
                        saatKembali = { tutupEditor() },
                        saatBuangPerubahan = {
                            editorPenyimpan.sembunyikanDialogTutup()
                            berkasDibuka = null
                        },
                        saatBatalTutup = { editorPenyimpan.sembunyikanDialogTutup() }
                    )
                } else {
                    DaftarBerkasLayar(state) { item ->
                        if (item.adalahFolder) {
                            penyimpan.bukaItem(item.pengenal, item.nama, item.uriAtauPath, true)
                        } else {
                            scope.launch {
                                val lokasi = LokasiBerkas(item.pengenal, item.nama, item.uriAtauPath, false)
                                val konten = repositori.baca(lokasi)
                                editorPenyimpan.bukaBerkas(item.nama, konten.teks)
                                versiDibuka = konten.versiEksternal
                                berkasDibuka = lokasi
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buatDataPercontoh() {
        val dokumen = java.io.File(filesDir, "Dokumen")
        if (!dokumen.exists()) {
            dokumen.mkdir()
            java.io.File(dokumen, "contoh.txt").writeText("Selamat datang di Crouton Alpha.")
        }
        val proyek = java.io.File(filesDir, "Proyek")
        if (!proyek.exists()) {
            proyek.mkdir()
            java.io.File(proyek, "Main.kt").writeText("fun main() {}")
        }
        val selamatDatang = java.io.File(filesDir, "selamat-datang.txt")
        if (!selamatDatang.exists()) {
            selamatDatang.writeText("Ini penyimpanan internal aplikasi (filesDir).")
        }
    }
}
