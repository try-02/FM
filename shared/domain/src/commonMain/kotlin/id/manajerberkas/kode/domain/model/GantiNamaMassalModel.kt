package id.manajerberkas.kode.domain.model
data class ItemGantiNama(
    val lokasi: LokasiBerkas,
    val namaLama: String
)
data class KueriGantiNamaMassal(
    val pola: String,
    val nomorAwal: Int = 1
)
data class HasilPratinjauGantiNama(
    val lokasi: LokasiBerkas,
    val namaLama: String,
    val namaBaru: String?,
    val pesanError: String? = null,
    val konflik: Boolean = false
) {
    val valid: Boolean
        get() = namaBaru != null && pesanError == null && !konflik
}
