package com.crouton.alpha.presentation

import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.repository.RepositoriBerkas
import com.crouton.alpha.domain.usecase.BandingkanIsiBerkasUseCase
import com.crouton.alpha.domain.util.DiffBerkas
import com.crouton.alpha.domain.util.JenisPerbedaan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PenyimpanStateDiff(
    private val repositori: RepositoriBerkas,
    private val scope: CoroutineScope,
    private val bandingkanUseCase: BandingkanIsiBerkasUseCase = BandingkanIsiBerkasUseCase()
) {
    private val _state = MutableStateFlow(StateDiff())
    val state: StateFlow<StateDiff> = _state

    private val pilihan = mutableListOf<LokasiBerkas>()

    // Pilih file untuk diff; maks 2. Mengembalikan true bila pilihan lengkap.
    fun pilihFile(lokasi: LokasiBerkas): Boolean {
        if (pilihan.size >= 2) pilihan.removeAt(0)
        pilihan.add(lokasi)
        _state.value = _state.value.copy(terpilih = pilihan.map { it.namaTampil })
        return pilihan.size == 2
    }

    fun bersihkanPilihan() {
        pilihan.clear()
        _state.value = _state.value.copy(terpilih = emptyList(), barisDiff = emptyList(), jumlahPerubahan = 0)
    }

    fun bandingkan() {
        if (pilihan.size < 2) {
            _state.value = _state.value.copy(pesan = "Pilih 2 file untuk dibandingkan")
            return
        }
        _state.value = _state.value.copy(sedangMemuat = true, pesan = "")
        scope.launch {
            try {
                val teksA = repositori.baca(pilihan[0]).teks
                val teksB = repositori.baca(pilihan[1]).teks
                val hasil = bandingkanUseCase.jalankan(teksA, teksB)
                val barisTampil = hasil.baris.map { baris ->
                    val simbol = when (baris.jenis) {
                        JenisPerbedaan.TAMBAH -> "+"
                        JenisPerbedaan.HAPUS -> "-"
                        JenisPerbedaan.SAMA -> " "
                    }
                    val nomor = if (!state.value.modeUnified) {
                        "A:" + (baris.nomorBarisA?.toString() ?: "-") + " B:" + (baris.nomorBarisB?.toString() ?: "-")
                    } else {
                        "A:" + (baris.nomorBarisA?.toString() ?: "-") + " B:" + (baris.nomorBarisB?.toString() ?: "-")
                    }
                    simbol + " [" + nomor + "] " + baris.teks
                }
                _state.value = _state.value.copy(
                    sedangMemuat = false,
                    jumlahPerubahan = hasil.jumlahPerubahan,
                    indeksPerubahanAktif = 0,
                    barisDiff = barisTampil,
                    pesan = if (hasil.identik) "Tidak ada perubahan" else hasil.jumlahPerubahan.toString() + " perubahan"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    sedangMemuat = false,
                    pesan = "Gagal membandingkan: " + (e.message ?: "kesalahan tidak diketahui")
                )
            }
        }
    }

    fun perubahanBerikutnya() {
        val maks = _state.value.barisDiff.size
        if (maks == 0) return
        _state.value = _state.value.copy(
            indeksPerubahanAktif = (_state.value.indeksPerubahanAktif + 1) % maks
        )
    }

    fun perubahanSebelumnya() {
        val maks = _state.value.barisDiff.size
        if (maks == 0) return
        _state.value = _state.value.copy(
            indeksPerubahanAktif = (_state.value.indeksPerubahanAktif - 1 + maks) % maks
        )
    }

    fun alihModeUnified() {
        _state.value = _state.value.copy(modeUnified = !state.value.modeUnified)
    }
}
