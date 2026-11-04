package id.manajerberkas.kode.domain.model

enum class JenisOperasiDiff {
    SAMA,
    HAPUS,
    TAMBAH
}

data class BarisDiff(
    val jenis: JenisOperasiDiff,
    val isi: String,
    val nomorBarisA: Int?,
    val nomorBarisB: Int?
)

data class HasilDiff(
    val baris: List<BarisDiff>,
    val jumlahPerubahan: Int,
    val identik: Boolean
)