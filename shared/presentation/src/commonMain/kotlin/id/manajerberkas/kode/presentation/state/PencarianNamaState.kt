package id.manajerberkas.kode.presentation.state
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarian
data class PencarianNamaState(
    val lokasiPencarian: LokasiBerkas? = null,
    val teks: String = "",
    val mode: ModePencarian = ModePencarian.REKURSIF,
    val pekaHuruf: Boolean = false,
    val ekstensi: String = "",
    val ukuranMinKb: String = "",
    val ukuranMaxKb: String = "",
    val hanyaFolder: Boolean? = null,
    val sedangMencari: Boolean = false,
    val hasil: List<ItemBerkas> = emptyList(),
    val pesan: String? = null,
    val pernahMencari: Boolean = false
)
