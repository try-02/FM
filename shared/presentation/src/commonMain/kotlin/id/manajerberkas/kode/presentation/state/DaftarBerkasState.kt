package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.KemampuanPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MarkahLokasi

enum class ModePilihTujuan {
    SALIN,
    PINDAH
}

data class DaftarBerkasState(
    val memuat: Boolean = false,
    val lokasiAktif: LokasiBerkas? = null,
    val daftar: List<ItemBerkas> = emptyList(),
    val pesan: String? = null,
    val kemampuan: KemampuanPenyimpanan? = null,
    val bisaKembali: Boolean = false,
    val sedangOperasi: Boolean = false,

    val modePilih: Boolean = false,
    val itemTerpilih: Set<ItemBerkas> = emptySet(),

    val menuUntukItem: ItemBerkas? = null,

    val dialogBuatBerkas: Boolean = false,
    val dialogBuatFolder: Boolean = false,
    val dialogGantiNama: ItemBerkas? = null,

    val konfirmasiHapusTerpilih: Boolean = false,
    val modePilihTujuan: ModePilihTujuan? = null,

    val editorTujuan: TujuanEditor? = null,

    val daftarMarkah: List<MarkahLokasi> = emptyList(),
    val dialogMarkah: Boolean = false,
    val beranda: LokasiBerkas? = null,

    val pencarianTerbuka: Boolean = false

    val tujuanDiff: TujuanDiff? = null

    val pencarianIsiTerbuka: Boolean = false

    val tujuanGantiNamaMassal: List<id.manajerberkas.kode.domain.model.ItemGantiNama>? = null

    val tujuanArsip: LokasiBerkas? = null
) {
    val jumlahTerpilih: Int
        get() = itemTerpilih.size

    val satuItemTerpilih: ItemBerkas?
        get() = if (itemTerpilih.size == 1) itemTerpilih.firstOrNull() else null

    val lokasiAktifSudahMarkah: Boolean
        get() = lokasiAktif != null &&
            daftarMarkah.any { it.lokasi.id == lokasiAktif.id }

    val lokasiAktifAdalahBeranda: Boolean
        get() = lokasiAktif != null &&
            beranda?.id == lokasiAktif.id
}