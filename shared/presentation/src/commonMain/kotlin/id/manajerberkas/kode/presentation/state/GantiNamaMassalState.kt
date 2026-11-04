package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.HasilPratinjauGantiNama
import id.manajerberkas.kode.domain.model.ItemGantiNama

data class GantiNamaMassalState(
    val items: List<ItemGantiNama> = emptyList(),
    val pola: String = "",
    val nomorAwal: String = "1",
    val pratinjau: List<HasilPratinjauGantiNama> = emptyList(),
    val sedangMemproses: Boolean = false,
    val pesan: String? = null,
    val hasilEksekusi: List<HasilOperasi>? = null
) {
    val jumlahValid: Int
        get() = pratinjau.count { it.valid }

    val jumlahItem: Int
        get() = items.size
}