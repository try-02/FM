package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.KecocokanIsiFile
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ModePencarianIsi

data class PencarianIsiState(
    val lokasiPencarian: LokasiBerkas? = null,
    val teks: String = "",
    val mode: ModePencarianIsi = ModePencarianIsi.REKURSIF,
    val pekaHuruf: Boolean = false,
    val kataUtuh: Boolean = false,
    val gunakanRegex: Boolean = false,
    val ekstensi: String = "",
    val ukuranMinKb: String = "",
    val ukuranMaxKb: String = "",
    val sedangMencari: Boolean = false,
    val hasil: List<KecocokanIsiFile> = emptyList(),
    val pesan: String? = null,
    val jumlahFileDiproses: Int = 0,
    val jumlahKecocokanTotal: Int = 0,
    val pernahMencari: Boolean = false
)