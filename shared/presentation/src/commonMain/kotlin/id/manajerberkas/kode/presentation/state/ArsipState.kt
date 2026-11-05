package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.ItemArsip
import id.manajerberkas.kode.domain.model.KemampuanFormatArsip
import id.manajerberkas.kode.domain.model.LokasiBerkas
data class ArsipState(
    val memuat: Boolean = false,
    val lokasiArsip: LokasiBerkas? = null,
    val jalurAktif: String = "",
    val daftar: List<ItemArsip> = emptyList(),
    val daftarTampil: List<ItemArsip> = emptyList(),
    val kemampuan: KemampuanFormatArsip? = null,
    val pesan: String? = null,
    val sedangEkstrak: Boolean = false,
    val bisaKembali: Boolean = false,
    val itemTerpilih: Set<String> = emptySet(),
    val modePilih: Boolean = false
)
