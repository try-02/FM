package id.manajerberkas.kode.domain.model

enum class JenisItem { BERKAS, FOLDER }

data class ItemBerkas(
    val nama: String,
    val lokasi: LokasiBerkas,
    val jenis: JenisItem,
    val ukuranByte: Long?,
    val waktuModifikasi: Long? // Epoch millis
)