package id.manajerberkas.kode.domain.model
data class HasilDaftarBerkas(
    val sukses: Boolean,
    val pesan: String? = null,
    val daftar: List<ItemBerkas> = emptyList()
)
