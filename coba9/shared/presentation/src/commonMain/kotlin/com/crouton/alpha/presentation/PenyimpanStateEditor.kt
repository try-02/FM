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

    fun tandaiGagal(pesan: String) {
        _state.value = _state.value.copy(status = StatusEditor.GAGAL)
    }

    fun tambahRiwayatPencarian(kata: String) {
        val lama = _state.value.riwayatPencarian.filterNot { it == kata }
        _state.value = _state.value.copy(riwayatPencarian = (listOf(kata) + lama).take(20))
    }

    fun hapusRiwayatPencarian() {
        _state.value = _state.value.copy(riwayatPencarian = emptyList())
    }
}
