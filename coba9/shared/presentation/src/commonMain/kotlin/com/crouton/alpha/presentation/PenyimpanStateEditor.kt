package com.crouton.alpha.presentation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PenyimpanStateEditor {
    private val _state = MutableStateFlow(StateEditor())
    val state: StateFlow<StateEditor> = _state

    fun ubahTeks(teksBaru: String) {
        _state.value = _state.value.copy(teks = teksBaru, status = StatusEditor.BERUBAH)
    }

    fun menujuBaris(nomorBaris: Int, jumlahBaris: Int): Boolean {
        if (nomorBaris < 1 || nomorBaris > jumlahBaris) return false
        _state.value = _state.value.copy(nomorBarisAktif = nomorBaris)
        return true
    }

    fun tandaiTersimpan() {
        _state.value = _state.value.copy(status = StatusEditor.TERSIMPAN)
    }

    fun bukaBerkas(namaBerkas: String, teks: String, hanyaBaca: Boolean = false) {
        _state.value = StateEditor(
            namaBerkas = namaBerkas,
            teks = teks,
            status = StatusEditor.TERSIMPAN,
            hanyaBaca = hanyaBaca
        )
    }

    fun tampilkanDialogTutup() {
        _state.value = _state.value.copy(tampilkanDialogTutup = true)
    }

    fun sembunyikanDialogTutup() {
        _state.value = _state.value.copy(tampilkanDialogTutup = false)
    }

    fun tandaiGagal(pesan: String) {
        _state.value = _state.value.copy(status = StatusEditor.GAGAL)
    }

    // Cari kemunculan berikutnya dari posisi saat ini; menggeser posisi per kata ditemukan.
    fun cariBerikutnya(kata: String): Boolean {
        if (kata.isEmpty()) return false
        val teks = _state.value.teks
        val mulai = (_state.value.posisiPencarian + 1).coerceAtMost(teks.length)
        var indeks = teks.indexOf(kata, mulai)
        if (indeks < 0) indeks = teks.indexOf(kata)
        if (indeks < 0) {
            _state.value = _state.value.copy(pesanStatus = "Tidak ditemukan: $kata")
            return false
        }
        val baris = teks.substring(0, indeks).count { it == '\n' } + 1
        _state.value = _state.value.copy(
            posisiPencarian = indeks,
            nomorBarisAktif = baris,
            pesanStatus = "Ditemukan di baris $baris"
        )
        return true
    }

    fun cariSebelumnya(kata: String): Boolean {
        if (kata.isEmpty()) return false
        val teks = _state.value.teks
        val mulai = (_state.value.posisiPencarian - 1).coerceAtLeast(0)
        var indeks = teks.lastIndexOf(kata, mulai)
        if (indeks < 0) indeks = teks.lastIndexOf(kata)
        if (indeks < 0) {
            _state.value = _state.value.copy(pesanStatus = "Tidak ditemukan: $kata")
            return false
        }
        val baris = teks.substring(0, indeks).count { it == '\n' } + 1
        _state.value = _state.value.copy(
            posisiPencarian = indeks,
            nomorBarisAktif = baris,
            pesanStatus = "Ditemukan di baris $baris"
        )
        return true
    }

    // Ganti tepat satu kecocokan pertama; mengembalikan jumlah yang diganti (0/1).
    fun gantiSatu(kata: String, pengganti: String): Int {
        if (kata.isEmpty()) return 0
        val teks = _state.value.teks
        val indeks = teks.indexOf(kata)
        if (indeks < 0) {
            _state.value = _state.value.copy(pesanStatus = "Tidak ditemukan: $kata")
            return 0
        }
        val baru = teks.substring(0, indeks) + pengganti + teks.substring(indeks + kata.length)
        _state.value = _state.value.copy(
            teks = baru,
            status = StatusEditor.BERUBAH,
            posisiPencarian = indeks + pengganti.length,
            pesanStatus = "1 kecocokan diganti"
        )
        return 1
    }

    // Ganti semua kecocokan; mengembalikan jumlah perubahan.
    fun gantiSemua(kata: String, pengganti: String): Int {
        if (kata.isEmpty()) return 0
        val teks = _state.value.teks
        val jumlah = teks.split(kata).size - 1
        if (jumlah <= 0) {
            _state.value = _state.value.copy(pesanStatus = "Tidak ditemukan: $kata")
            return 0
        }
        val baru = teks.replace(kata, pengganti)
        _state.value = _state.value.copy(
            teks = baru,
            status = StatusEditor.BERUBAH,
            posisiPencarian = 0,
            pesanStatus = "$jumlah kecocokan diganti"
        )
        return jumlah
    }

    fun tambahRiwayatPencarian(kata: String) {
        val lama = _state.value.riwayatPencarian.filterNot { it == kata }
        _state.value = _state.value.copy(riwayatPencarian = (listOf(kata) + lama).take(20))
    }

    fun hapusRiwayatPencarian() {
        _state.value = _state.value.copy(riwayatPencarian = emptyList())
    }
}
