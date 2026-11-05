package id.manajerberkas.kode.presentation.state
import androidx.compose.ui.text.input.TextFieldValue
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.StatistikTeks
data class EditorState(
    val memuat: Boolean = false,
    val lokasi: LokasiBerkas? = null,
    val hanyaBaca: Boolean = false,
    val sedangMenyimpan: Boolean = false,
    val teksAwal: String = "",
    val medan: TextFieldValue = TextFieldValue(),
    val pesan: String? = null,
    val konfirmasiTutup: Boolean = false,
    val cari: String = "",
    val ganti: String = "",
    val pekaHuruf: Boolean = false,
    val kataUtuh: Boolean = false,
    val gunakanRegex: Boolean = false,
    val encoding: String? = null,
    val panelStatistikTerbuka: Boolean = false,
    val sedangMenghitungStatistik: Boolean = false,
    val statistik: StatistikTeks? = null,
    val dialogNavigasiBaris: Boolean = false,
    val nomorBarisInput: String = "",
    val barisAktif: Int = 1,
    val kolomAktif: Int = 1,
    val bungkusKata: Boolean = true,
    val riwayatAktif: Boolean = true,
    val bisaUndo: Boolean = false,
    val bisaRedo: Boolean = false,
    val riwayatCari: List<String> = emptyList(),
    val dialogRiwayatCari: Boolean = false,
    val konfirmasiKonflikEksternal: Boolean = false
) {
    val adaPerubahan: Boolean
        get() = medan.text != teksAwal
}
