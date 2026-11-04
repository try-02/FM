package id.manajerberkas.kode.presentation.state

import androidx.compose.ui.text.input.TextFieldValue

interface EditorAksi {
    fun ubahTeks(medan: TextFieldValue)

    fun simpan()

    fun mintaTutup()
    fun tutupTanpaSimpan()
    fun simpanDanTutup()
    fun batalTutup()

    fun undo()
    fun redo()

    fun ubahCari(nilai: String)
    fun ubahGanti(nilai: String)

    fun togglePekaHuruf()
    fun toggleKataUtuh()
    fun toggleRegex()
    fun toggleBungkusKata()

    fun cariBerikutnya()
    fun cariSebelumnya()

    fun gantiSatu()
    fun gantiSemua()

    fun bukaPanelStatistik()
    fun tutupPanelStatistik()
    fun hitungStatistik()

    fun bukaDialogNavigasiBaris()
    fun tutupDialogNavigasiBaris()
    fun ubahNomorBarisInput(nilai: String)
    fun konfirmasiNavigasiBaris()

    fun bukaRiwayatCari()
    fun tutupRiwayatCari()
    fun pilihRiwayatCari(nilai: String)
    fun hapusRiwayatCari()

    fun timpaKonflikEksternal()
    fun muatUlangKonflikEksternal()
    fun batalKonflikEksternal()
}