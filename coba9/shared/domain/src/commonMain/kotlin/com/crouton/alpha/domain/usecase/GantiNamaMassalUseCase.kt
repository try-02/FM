package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.util.TokenGantiNama
import com.crouton.alpha.domain.util.ValidasiNamaBerkas

data class PratinjauGantiNama(val namaLama: String, val namaBaru: String, val valid: Boolean, val alasan: String = "")

class GantiNamaMassalUseCase {
    fun buatPratinjau(pola: String, daftarNama: List<String>, nomorAwal: Int): List<PratinjauGantiNama> {
        val hasil = daftarNama.mapIndexed { indeks, nama ->
            val baru = TokenGantiNama.terapkanPola(pola, nama, nomorAwal + indeks)
            val valid = ValidasiNamaBerkas.adalahNamaValid(baru)
            PratinjauGantiNama(nama, baru, valid, ValidasiNamaBerkas.pesanKesalahan(baru))
        }
        val konflik = TokenGantiNama.deteksiKonflik(hasil.map { it.namaBaru })
        return hasil.map { item ->
            if (item.namaBaru in konflik) item.copy(valid = false, alasan = "Nama bentrok dengan item lain")
            else item
        }
    }
}
