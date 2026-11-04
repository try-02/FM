package id.manajerberkas.kode.domain.model

enum class JenisItemArsip {
    BERKAS,
    FOLDER
}

data class ItemArsip(
    val jalur: String,
    val nama: String,
    val jenis: JenisItemArsip,
    val ukuran: Long,
    val ukuranTerkompresi: Long?,
    val waktuModifikasi: Long?
)

data class KemampuanFormatArsip(
    val format: String,
    val bisaBaca: Boolean,
    val bisaEkstrak: Boolean,
    val bisaBuat: Boolean,
    val bisaUbahIsi: Boolean,
    val dukungEnkripsi: Boolean
)

data class HasilDaftarArsip(
    val sukses: Boolean,
    val pesan: String? = null,
    val daftar: List<ItemArsip> = emptyList(),
    val kemampuan: KemampuanFormatArsip? = null
)

data class HasilEkstraksi(
    val sukses: Boolean,
    val pesan: String? = null,
    val jumlahBerhasil: Int = 0,
    val jumlahGagal: Int = 0
)