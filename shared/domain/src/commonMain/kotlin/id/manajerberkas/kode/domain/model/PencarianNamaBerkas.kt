package id.manajerberkas.kode.domain.model

enum class ModePencarian {
    FOLDER_SAAT_INI,
    REKURSIF
}

data class KueriPencarianNama(
    val teks: String,
    val mode: ModePencarian = ModePencarian.FOLDER_SAAT_INI,
    val pekaHuruf: Boolean = false,
    val ekstensi: String? = null,
    val ukuranMin: Long? = null,
    val ukuranMax: Long? = null,
    val hanyaFolder: Boolean? = null
)

data class HasilPencarianNama(
    val daftar: List<ItemBerkas> = emptyList(),
    val selesai: Boolean = false,
    val pesan: String? = null
)