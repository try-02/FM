package com.crouton.alpha.presentation

import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.usecase.DaftarIsiUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PenyimpanStateBerkas(
    private val daftarIsiUseCase: DaftarIsiUseCase,
    private val scope: CoroutineScope
) {
    private val _state = MutableStateFlow(StateLayarBerkas(sedangMemuat = true))
    val state: StateFlow<StateLayarBerkas> = _state

    private val tumpukanLokasi = ArrayDeque<LokasiBerkas>()

    fun muatAwal(lokasiDasar: LokasiBerkas) {
        tumpukanLokasi.clear()
        tumpukanLokasi.addLast(lokasiDasar)
        muat(lokasiDasar)
    }

    fun bukaItem(pengenal: String, namaTampil: String, uriAtauPath: String, adalahFolder: Boolean) {
        if (!adalahFolder) {
            _state.value = _state.value.copy(pesan = "Buka berkas: $namaTampil (editor menyusul)")
            return
        }
        val lokasi = LokasiBerkas(pengenal, namaTampil, uriAtauPath, true)
        tumpukanLokasi.addLast(lokasi)
        muat(lokasi)
    }

    fun kembali() {
        if (tumpukanLokasi.size <= 1) return
        tumpukanLokasi.removeLast()
        muat(tumpukanLokasi.last())
    }

    private fun muat(lokasi: LokasiBerkas) {
        _state.value = _state.value.copy(sedangMemuat = true, lokasiAktif = lokasi.namaTampil, pesan = "")
        scope.launch {
            try {
                val hasil = daftarIsiUseCase.jalankan(lokasi)
                val urut = hasil.isi.sortedWith(
                    compareByDescending<com.crouton.alpha.domain.model.EntriBerkas> { it.jenis == JenisEntri.FOLDER }
                        .thenBy { it.lokasi.namaTampil.lowercase() }
                )
                val daftarUi = urut.map { entri ->
                    StatusBerkasUi(
                        nama = entri.lokasi.namaTampil,
                        ukuranTampil = formatUkuran(entri.ukuranByte),
                        tersembunyi = entri.tersembunyi,
                        pengenal = entri.lokasi.pengenal,
                        uriAtauPath = entri.lokasi.uriAtauPath,
                        adalahFolder = entri.jenis == JenisEntri.FOLDER
                    )
                }
                _state.value = _state.value.copy(
                    sedangMemuat = false,
                    daftar = daftarUi,
                    lokasiAktif = lokasi.uriAtauPath,
                    pesan = if (daftarUi.isEmpty()) "Folder kosong." else ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    sedangMemuat = false,
                    pesan = "Gagal memuat folder: " + (e.message ?: "kesalahan tidak diketahui")
                )
            }
        }
    }

    private fun formatUkuran(byte: Long): String {
        return when {
            byte < 1024 -> byte.toString() + " B"
            byte < 1024 * 1024 -> (byte / 1024).toString() + " KB"
            else -> (byte / (1024 * 1024)).toString() + " MB"
        }
    }
}
