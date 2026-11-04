package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.ItemBerkas
import id.manajerberkas.kode.domain.model.JenisItem
import id.manajerberkas.kode.domain.model.JenisMarkah
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MarkahLokasi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DaftarBerkasStateHolder(
    private val dependensi: DependensiPenjelajah,
    private val scope: CoroutineScope
) : DaftarBerkasAksi {

    private val _state = MutableStateFlow(DaftarBerkasState())
    val state: StateFlow<DaftarBerkasState> = _state

    private val tumpukanLokasi = ArrayDeque<LokasiBerkas>()
    private var pekerjaanMuat: Job? = null

    init {
        muatMarkah()
    }

    fun bukaAwalBeranda(lokasiBawaan: LokasiBerkas) {
        scope.launch {
            val beranda = dependensi.ambilBeranda()

            _state.update {
                it.copy(beranda = beranda)
            }

            bukaAwal(beranda ?: lokasiBawaan)
        }
    }

    fun bukaAwal(lokasi: LokasiBerkas) {
        tumpukanLokasi.clear()
        tumpukanLokasi.addLast(lokasi)

        _state.update {
            it.copy(
                modePilih = false,
                itemTerpilih = emptySet(),
                menuUntukItem = null,
                dialogBuatBerkas = false,
                dialogBuatFolder = false,
                dialogGantiNama = null,
                konfirmasiHapusTerpilih = false,
                modePilihTujuan = null,
                editorTujuan = null,
                dialogMarkah = false
            )
        }

        muatLokasiAktif()
    }

    fun selesaiBukaEditor() {
        _state.update {
            it.copy(editorTujuan = null)
        }
    }

    override fun kembali() {
        val kondisi = state.value

        when {
            kondisi.modePilihTujuan != null -> batalPilihTujuan()
            kondisi.modePilih -> batalkanPilih()
            kondisi.menuUntukItem != null -> tutupMenu()
            kondisi.dialogBuatBerkas -> tutupDialogBuatBerkas()
            kondisi.dialogBuatFolder -> tutupDialogBuatFolder()
            kondisi.dialogGantiNama != null -> tutupDialogGantiNama()
            kondisi.konfirmasiHapusTerpilih -> batalHapus()
            kondisi.dialogMarkah -> tutupDialogMarkah()

            else -> {
                if (tumpukanLokasi.size <= 1) return

                tumpukanLokasi.removeLast()

                _state.update {
                    it.copy(
                        modePilih = false,
                        itemTerpilih = emptySet(),
                        menuUntukItem = null
                    )
                }

                muatLokasiAktif()
            }
        }
    }

    override fun klikItem(item: ItemBerkas) {
        val kondisi = state.value

        when {
            kondisi.modePilihTujuan != null -> {
                if (item.jenis == JenisItem.FOLDER) {
                    tumpukanLokasi.addLast(item.lokasi)
                    muatLokasiAktif()
                }
            }

            kondisi.modePilih -> togglePilih(item)

            item.jenis == JenisItem.FOLDER -> {
                tumpukanLokasi.addLast(item.lokasi)
                muatLokasiAktif()
            }

            else -> {
                _state.update {
                    it.copy(
                        editorTujuan = TujuanEditor(
                            lokasi = item.lokasi
                        )
                    )
                }
            }
        }
    }

    override fun tekanLama(item: ItemBerkas) {
        if (state.value.modePilihTujuan != null) return

        val terpilih = state.value.itemTerpilih + item

        _state.update {
            it.copy(
                modePilih = true,
                itemTerpilih = terpilih,
                menuUntukItem = item
            )
        }
    }

    private fun togglePilih(item: ItemBerkas) {
        val sekarang = state.value.itemTerpilih
        val sudah = sekarang.any { it.lokasi.id == item.lokasi.id }

        val baru = if (sudah) {
            sekarang.filterNot { it.lokasi.id == item.lokasi.id }.toSet()
        } else {
            sekarang + item
        }

        _state.update {
            it.copy(
                itemTerpilih = baru,
                modePilih = baru.isNotEmpty()
            )
        }
    }

    override fun pilihSemua() {
        _state.update {
            it.copy(
                itemTerpilih = it.daftar.toSet(),
                modePilih = it.daftar.isNotEmpty()
            )
        }
    }

    override fun batalkanPilih() {
        _state.update {
            it.copy(
                modePilih = false,
                itemTerpilih = emptySet(),
                menuUntukItem = null
            )
        }
    }

    override fun bukaDialogBuatBerkas() {
        _state.update {
            it.copy(
                dialogBuatBerkas = true,
                dialogBuatFolder = false,
                pesan = null
            )
        }
    }

    override fun bukaDialogBuatFolder() {
        _state.update {
            it.copy(
                dialogBuatFolder = true,
                dialogBuatBerkas = false,
                pesan = null
            )
        }
    }

    override fun tutupDialogBuatBerkas() {
        _state.update { it.copy(dialogBuatBerkas = false) }
    }

    override fun tutupDialogBuatFolder() {
        _state.update { it.copy(dialogBuatFolder = false) }
    }

    override fun buatBerkas(nama: String) {
        val induk = lokasiAktif() ?: return

        _state.update { it.copy(dialogBuatBerkas = false) }

        jalankanOperasi {
            dependensi.buatBerkas(induk, nama)
        }
    }

    override fun buatFolder(nama: String) {
        val induk = lokasiAktif() ?: return

        _state.update { it.copy(dialogBuatFolder = false) }

        jalankanOperasi {
            dependensi.buatFolder(induk, nama)
        }
    }

    override fun bukaDialogGantiNama() {
        val item = state.value.satuItemTerpilih
            ?: state.value.menuUntukItem
            ?: return

        _state.update {
            it.copy(
                dialogGantiNama = item,
                menuUntukItem = null
            )
        }
    }

    override fun tutupDialogGantiNama() {
        _state.update { it.copy(dialogGantiNama = null) }
    }

    override fun gantiNama(namaBaru: String) {
        val item = state.value.dialogGantiNama ?: return

        _state.update { it.copy(dialogGantiNama = null) }

        jalankanOperasi {
            dependensi.gantiNama(item.lokasi, namaBaru)
        }
    }

    override fun mintaHapusTerpilih() {
        if (state.value.itemTerpilih.isEmpty()) return

        _state.update {
            it.copy(
                konfirmasiHapusTerpilih = true,
                menuUntukItem = null
            )
        }
    }

    override fun batalHapus() {
        _state.update { it.copy(konfirmasiHapusTerpilih = false) }
    }

    override fun konfirmasiHapus() {
        val daftarItem = state.value.itemTerpilih.toList()

        if (daftarItem.isEmpty()) return

        scope.launch {
            _state.update {
                it.copy(
                    sedangOperasi = true,
                    konfirmasiHapusTerpilih = false,
                    pesan = null
                )
            }

            var sukses = 0
            var gagal = 0
            var pesanGagal: String? = null

            daftarItem.forEach { item ->
                val hasil = dependensi.hapusBerkas(item.lokasi)

                if (hasil.sukses) {
                    sukses++
                } else {
                    gagal++
                    pesanGagal = hasil.pesan
                }
            }

            val pesanAkhir = when {
                gagal == 0 -> "$sukses item dihapus."
                sukses == 0 -> pesanGagal ?: "Gagal menghapus item."
                else -> "$sukses berhasil, $gagal gagal."
            }

            _state.update {
                it.copy(
                    sedangOperasi = false,
                    pesan = pesanAkhir,
                    modePilih = false,
                    itemTerpilih = emptySet(),
                    menuUntukItem = null
                )
            }

            if (sukses > 0) muatLokasiAktif()
        }
    }

    override fun mulaiSalin() {
        if (state.value.itemTerpilih.isEmpty()) return

        _state.update {
            it.copy(
                modePilihTujuan = ModePilihTujuan.SALIN,
                menuUntukItem = null
            )
        }
    }

    override fun mulaiPindah() {
        if (state.value.itemTerpilih.isEmpty()) return

        _state.update {
            it.copy(
                modePilihTujuan = ModePilihTujuan.PINDAH,
                menuUntukItem = null
            )
        }
    }

    override fun batalPilihTujuan() {
        _state.update {
            it.copy(
                modePilihTujuan = null,
                modePilih = it.itemTerpilih.isNotEmpty()
            )
        }
    }

    override fun konfirmasiTujuan() {
        val tujuan = lokasiAktif() ?: return
        val mode = state.value.modePilihTujuan ?: return
        val daftarItem = state.value.itemTerpilih.toList()

        if (daftarItem.isEmpty()) return

        scope.launch {
            _state.update {
                it.copy(
                    sedangOperasi = true,
                    pesan = null
                )
            }

            var sukses = 0
            var gagal = 0
            var pesanGagal: String? = null

            daftarItem.forEach { item ->
                val hasil = when (mode) {
                    ModePilihTujuan.SALIN -> dependensi.salinBerkas(item.lokasi, tujuan)
                    ModePilihTujuan.PINDAH -> dependensi.pindahkanBerkas(item.lokasi, tujuan)
                }

                if (hasil.sukses) {
                    sukses++
                } else {
                    gagal++
                    pesanGagal = hasil.pesan
                }
            }

            val kata = when (mode) {
                ModePilihTujuan.SALIN -> "disalin"
                ModePilihTujuan.PINDAH -> "dipindahkan"
            }

            val pesanAkhir = when {
                gagal == 0 -> "$sukses item $kata."
                sukses == 0 -> pesanGagal ?: "Operasi gagal."
                else -> "$sukses berhasil, $gagal gagal."
            }

            _state.update {
                it.copy(
                    sedangOperasi = false,
                    pesan = pesanAkhir,
                    modePilihTujuan = null,
                    modePilih = false,
                    itemTerpilih = emptySet(),
                    menuUntukItem = null
                )
            }

            if (sukses > 0) muatLokasiAktif()
        }
    }

    override fun tutupMenu() {
        _state.update { it.copy(menuUntukItem = null) }
    }

    override fun bukaItemDariMenu(item: ItemBerkas) {
        _state.update {
            it.copy(
                menuUntukItem = null,
                modePilih = false,
                itemTerpilih = emptySet()
            )
        }

        if (item.jenis == JenisItem.FOLDER) {
            tumpukanLokasi.addLast(item.lokasi)
            muatLokasiAktif()
        } else {
            _state.update {
                it.copy(
                    editorTujuan = TujuanEditor(lokasi = item.lokasi)
                )
            }
        }
    }

    override fun muatMarkah() {
        scope.launch {
            val daftar = dependensi.ambilMarkah()

            _state.update {
                it.copy(daftarMarkah = daftar)
            }
        }
    }

    override fun bukaDialogMarkah() {
        _state.update {
            it.copy(dialogMarkah = true)
        }

        muatMarkah()
    }

    override fun tutupDialogMarkah() {
        _state.update {
            it.copy(dialogMarkah = false)
        }
    }

    override fun tambahMarkahLokasiAktif() {
        val lokasi = lokasiAktif() ?: return

        scope.launch {
            dependensi.simpanMarkah(
                MarkahLokasi(
                    lokasi = lokasi,
                    jenis = JenisMarkah.FOLDER
                )
            )

            val daftar = dependensi.ambilMarkah()

            _state.update {
                it.copy(
                    daftarMarkah = daftar,
                    pesan = "Lokasi ditambahkan ke markah."
                )
            }
        }
    }

    override fun tambahMarkahItem(item: ItemBerkas) {
        scope.launch {
            dependensi.simpanMarkah(
                MarkahLokasi(
                    lokasi = item.lokasi,
                    jenis = if (item.jenis == JenisItem.FOLDER) {
                        JenisMarkah.FOLDER
                    } else {
                        JenisMarkah.BERKAS
                    }
                )
            )

            val daftar = dependensi.ambilMarkah()

            _state.update {
                it.copy(
                    daftarMarkah = daftar,
                    menuUntukItem = null,
                    modePilih = false,
                    itemTerpilih = emptySet(),
                    pesan = "Item ditambahkan ke markah."
                )
            }
        }
    }

    override fun hapusMarkah(markah: MarkahLokasi) {
        scope.launch {
            dependensi.hapusMarkah(markah.lokasi.id)

            val daftar = dependensi.ambilMarkah()

            _state.update {
                it.copy(
                    daftarMarkah = daftar,
                    pesan = "Markah dihapus."
                )
            }
        }
    }

    override fun bukaMarkah(markah: MarkahLokasi) {
        _state.update {
            it.copy(dialogMarkah = false)
        }

        if (markah.jenis == JenisMarkah.BERKAS) {
            _state.update {
                it.copy(
                    editorTujuan = TujuanEditor(lokasi = markah.lokasi)
                )
            }
        } else {
            bukaAwal(markah.lokasi)
        }
    }

    override fun jadikanBeranda() {
        val lokasi = lokasiAktif() ?: return

        scope.launch {
            dependensi.simpanBeranda(lokasi)

            _state.update {
                it.copy(
                    beranda = lokasi,
                    pesan = "Folder ini dijadikan beranda."
                )
            }
        }
    }

    override fun hapusBeranda() {
        scope.launch {
            dependensi.hapusBeranda()

            _state.update {
                it.copy(
                    beranda = null,
                    pesan = "Beranda dikembalikan ke lokasi bawaan."
                )
            }
        }
    }

    override fun bukaPencarian() {
        _state.update {
            it.copy(
                pencarianTerbuka = true,
                menuUntukItem = null,
                modePilih = false,
                itemTerpilih = emptySet()
            )
        }
    }

    override fun tutupPencarian() {
        _state.update {
            it.copy(pencarianTerbuka = false)
        }
    }

    fun bukaHasilPencarian(item: ItemBerkas) {
        if (item.jenis == JenisItem.FOLDER) {
            _state.update {
                it.copy(pencarianTerbuka = false)
            }

            bukaAwal(item.lokasi)
        } else {
            _state.update {
                it.copy(
                    editorTujuan = TujuanEditor(lokasi = item.lokasi)
                )
            }
        }
    }

    private fun lokasiAktif(): LokasiBerkas? = tumpukanLokasi.lastOrNull()

    private fun jalankanOperasi(
        operasi: suspend () -> HasilOperasi
    ) {
        scope.launch {
            _state.update {
                it.copy(
                    sedangOperasi = true,
                    pesan = null
                )
            }

            val hasil = operasi()

            _state.update {
                it.copy(
                    sedangOperasi = false,
                    pesan = hasil.pesan
                )
            }

            if (hasil.sukses) muatLokasiAktif()
        }
    }

    private fun muatLokasiAktif() {
        val lokasi = tumpukanLokasi.lastOrNull() ?: return

        pekerjaanMuat?.cancel()

        pekerjaanMuat = scope.launch {
            _state.update {
                it.copy(
                    memuat = true,
                    lokasiAktif = lokasi,
                    bisaKembali = tumpukanLokasi.size > 1
                )
            }

            val hasilDaftar = dependensi.daftarIsiFolder(lokasi)
            val kemampuan = dependensi.dapatkanKemampuan(lokasi)

            _state.update {
                it.copy(
                    memuat = false,
                    daftar = hasilDaftar.daftar,
                    pesan = if (hasilDaftar.sukses) null else hasilDaftar.pesan,
                    kemampuan = kemampuan
                )
            }
        }
    }
}