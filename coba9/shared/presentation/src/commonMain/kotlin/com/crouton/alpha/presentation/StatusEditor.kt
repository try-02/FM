package com.crouton.alpha.presentation

enum class StatusEditor { TERSIMPAN, BERUBAH, HANYA_BACA, MEMUAT, GAGAL }

data class StatusBerkasUi(
    val nama: String,
    val ukuranTampil: String,
    val tersembunyi: Boolean,
    val pengenal: String = "",
    val uriAtauPath: String = "",
    val adalahFolder: Boolean = false
)

data class StateLayarBerkas(
    val sedangMemuat: Boolean = false,
    val daftar: List<StatusBerkasUi> = emptyList(),
    val lokasiAktif: String = "",
    val pesan: String = "",
    val terpilih: Set<String> = emptySet(),
    val urutan: String = "NAMA"
)

data class StateEditor(
    val namaBerkas: String = "",
    val teks: String = "",
    val status: StatusEditor = StatusEditor.MEMUAT,
    val nomorBarisAktif: Int = 1,
    val bungkusKata: Boolean = true,
    val hanyaBaca: Boolean = false,
    val riwayatPencarian: List<String> = emptyList(),
    val tampilkanDialogTutup: Boolean = false
)

data class StateDiff(
    val sedangMemuat: Boolean = false,
    val modeUnified: Boolean = false,
    val jumlahPerubahan: Int = 0,
    val indeksPerubahanAktif: Int = 0,
    val pesan: String = ""
)

data class StateRename(
    val pola: String = "file_{N}{S}",
    val nomorAwal: Int = 1,
    val pratinjau: List<String> = emptyList(),
    val adaKonflik: Boolean = false,
    val pesan: String = ""
)
