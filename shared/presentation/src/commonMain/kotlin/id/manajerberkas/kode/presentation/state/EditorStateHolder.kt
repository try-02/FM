package id.manajerberkas.kode.presentation.state

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import id.manajerberkas.kode.domain.model.KontenTeks
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MetadataBerkas
import id.manajerberkas.kode.domain.usecase.BacaTeksUseCase
import id.manajerberkas.kode.domain.usecase.HitungStatistikTeksUseCase
import id.manajerberkas.kode.domain.usecase.SimpanTeksUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditorStateHolder(
    private val lokasi: LokasiBerkas,
    private val dependensi: DependensiEditor,
    private val scope: CoroutineScope,
    private val onTutup: () -> Unit,
    private val barisAwal: Int? = null
) : EditorAksi {

    private companion object {
        const val BATAS_POSISI_KURSOR = 200_000
        const val BATAS_UNDO = 300_000
        const val MAKS_UNDO = 20
        const val MAKS_REDO = 20
        const val MAKS_TOTAL_RIWAYAT_UNDO = 1_500_000L
        const val DEBOUNCE_RIWAYAT_MS = 700L
        const val MAKS_RIWAYAT_CARI = 20
    }

    private val _state = MutableStateFlow(
        EditorState(
            lokasi = lokasi
        )
    )

    val state: StateFlow<EditorState> = _state

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()

    private var totalUndoKarakter = 0L
    private var baselineTeks = ""
    private var riwayatAktifInternal = true

    private var metadataAwal: MetadataBerkas? = null
    private var tutupSetelahKonflikSelesai: Boolean = false

    private var pekerjaanRiwayat: Job? = null

    init {
        muatPreferensi()
        muatRiwayat()
        muatKonten()
    }

private fun muatKonten() {
    scope.launch {
        _state.update {
            it.copy(
                memuat = true,
                pesan = null,
                konfirmasiKonflikEksternal = false
            )
        }

        val hasil = dependensi.bacaTeks(lokasi)

        if (hasil.sukses && hasil.konten != null) {
            val teks = hasil.konten.teks

            var medanAwal = TextFieldValue(teks)
            var baris = 1
            var kolom = 1

            if (barisAwal != null) {
                val indeks = indeksAwalBaris(
                    teks = teks,
                    nomorBaris = barisAwal
                )

                if (indeks != null) {
                    medanAwal = TextFieldValue(
                        text = teks,
                        selection = TextRange(indeks)
                    )

                    baris = barisAwal
                    kolom = 1
                }
            }

            baselineTeks = teks
            riwayatAktifInternal = teks.length <= BATAS_UNDO

            undoStack.clear()
            redoStack.clear()
            totalUndoKarakter = 0L

            metadataAwal = dependensi.dapatkanMetadata(lokasi)

            _state.update {
                it.copy(
                    memuat = false,
                    hanyaBaca = hasil.hanyaBaca,
                    teksAwal = teks,
                    medan = medanAwal,
                    encoding = hasil.konten.encoding,
                    pesan = null,
                    barisAktif = baris,
                    kolomAktif = kolom,
                    riwayatAktif = riwayatAktifInternal,
                    konfirmasiKonflikEksternal = false
                )
            }

            perbaruiBisaUndoRedo()
        } else {
            riwayatAktifInternal = false
            metadataAwal = null

            _state.update {
                it.copy(
                    memuat = false,
                    hanyaBaca = true,
                    pesan = hasil.pesan ?: "Gagal membaca file.",
                    riwayatAktif = false,
                    konfirmasiKonflikEksternal = false
                )
            }

            perbaruiBisaUndoRedo()
        }
    }
}

    override fun ubahTeks(medan: TextFieldValue) {
        _state.update {
            it.copy(medan = medan)
        }

        perbaruiPosisiKursor(medan)

        if (medan.text.length > BATAS_UNDO) {
            if (riwayatAktifInternal) {
                riwayatAktifInternal = false

                undoStack.clear()
                redoStack.clear()
                totalUndoKarakter = 0L

                pekerjaanRiwayat?.cancel()

                baselineTeks = medan.text

                _state.update {
                    it.copy(
                        riwayatAktif = false,
                        pesan = "Riwayat undo/redo dinonaktifkan untuk teks besar."
                    )
                }

                perbaruiBisaUndoRedo()
            }

            return
        }

        if (riwayatAktifInternal) {
            perbaruiBisaUndoRedo()
            jadwalkanKomitRiwayat()
        }
    }

    private fun jadwalkanKomitRiwayat() {
        pekerjaanRiwayat?.cancel()

        pekerjaanRiwayat = scope.launch {
            delay(DEBOUNCE_RIWAYAT_MS)
            komitRiwayat()
        }
    }

    private fun komitRiwayat() {
        if (!riwayatAktifInternal) return

        val teksSekarang = state.value.medan.text

        if (teksSekarang == baselineTeks) return

        tambahKeUndo(baselineTeks)

        redoStack.clear()

        baselineTeks = teksSekarang

        perbaruiBisaUndoRedo()
    }

    private fun siapkanRiwayatSebelumOperasi() {
        pekerjaanRiwayat?.cancel()

        if (!riwayatAktifInternal) return

        val teksSekarang = state.value.medan.text

        if (teksSekarang != baselineTeks) {
            tambahKeUndo(baselineTeks)

            redoStack.clear()

            baselineTeks = teksSekarang

            perbaruiBisaUndoRedo()
        }
    }

    private fun terapkanTeksDenganRiwayat(
        teksBaru: String,
        seleksi: TextRange
    ) {
        if (teksBaru.length > BATAS_UNDO) {
            riwayatAktifInternal = false

            undoStack.clear()
            redoStack.clear()
            totalUndoKarakter = 0L

            pekerjaanRiwayat?.cancel()

            baselineTeks = teksBaru

            _state.update {
                it.copy(
                    medan = TextFieldValue(
                        text = teksBaru,
                        selection = seleksi
                    ),
                    riwayatAktif = false,
                    pesan = "Riwayat undo/redo dinonaktifkan untuk teks besar."
                )
            }

            perbaruiPosisiKursor(state.value.medan)
            perbaruiBisaUndoRedo()

            return
        }

        if (!riwayatAktifInternal) {
            baselineTeks = teksBaru

            _state.update {
                it.copy(
                    medan = TextFieldValue(
                        text = teksBaru,
                        selection = seleksi
                    )
                )
            }

            perbaruiPosisiKursor(state.value.medan)

            return
        }

        siapkanRiwayatSebelumOperasi()

        val teksLama = state.value.medan.text

        if (teksLama != teksBaru) {
            tambahKeUndo(teksLama)
            redoStack.clear()
        }

        _state.update {
            it.copy(
                medan = TextFieldValue(
                    text = teksBaru,
                    selection = seleksi
                )
            )
        }

        baselineTeks = teksBaru

        perbaruiPosisiKursor(state.value.medan)
        perbaruiBisaUndoRedo()
    }

    private fun tambahKeUndo(teks: String) {
        if (!riwayatAktifInternal) return
        if (teks.length > BATAS_UNDO) return

        undoStack.addLast(teks)
        totalUndoKarakter += teks.length

        while (
            undoStack.size > MAKS_UNDO ||
            totalUndoKarakter > MAKS_TOTAL_RIWAYAT_UNDO
        ) {
            val buang = undoStack.removeFirstOrNull() ?: break
            totalUndoKarakter -= buang.length
        }
    }

    private fun tambahKeRedo(teks: String) {
        if (!riwayatAktifInternal) return
        if (teks.length > BATAS_UNDO) return

        redoStack.addLast(teks)

        while (redoStack.size > MAKS_REDO) {
            redoStack.removeFirstOrNull()
        }
    }

    private fun ambilDariUndo(): String? {
        val teks = undoStack.removeLastOrNull() ?: return null

        totalUndoKarakter -= teks.length

        return teks
    }

    private fun perbaruiBisaUndoRedo() {
        val kondisi = state.value

        val bisaUndo = riwayatAktifInternal &&
            (
                undoStack.isNotEmpty() ||
                    kondisi.medan.text != baselineTeks
                )

        val bisaRedo = riwayatAktifInternal &&
            redoStack.isNotEmpty()

        _state.update {
            it.copy(
                riwayatAktif = riwayatAktifInternal,
                bisaUndo = bisaUndo,
                bisaRedo = bisaRedo
            )
        }
    }

    override fun undo() {
        if (!riwayatAktifInternal) return

        pekerjaanRiwayat?.cancel()

        val teksSekarang = state.value.medan.text

        val target = if (teksSekarang != baselineTeks) {
            baselineTeks
        } else {
            ambilDariUndo() ?: return
        }

        tambahKeRedo(teksSekarang)

        _state.update {
            it.copy(
                medan = TextFieldValue(
                    text = target,
                    selection = TextRange(target.length)
                )
            )
        }

        baselineTeks = target

        perbaruiPosisiKursor(state.value.medan)
        perbaruiBisaUndoRedo()
    }

    override fun redo() {
        if (!riwayatAktifInternal) return

        pekerjaanRiwayat?.cancel()

        val target = redoStack.removeLastOrNull() ?: return

        val teksSekarang = state.value.medan.text

        tambahKeUndo(teksSekarang)

        _state.update {
            it.copy(
                medan = TextFieldValue(
                    text = target,
                    selection = TextRange(target.length)
                )
            )
        }

        baselineTeks = target

        perbaruiPosisiKursor(state.value.medan)
        perbaruiBisaUndoRedo()
    }

override fun simpan() {
    scope.launch {
        tutupSetelahKonflikSelesai = false
        simpanDenganPemeriksaanKonflik(paksa = false)
    }
}

override fun simpanDanTutup() {
    scope.launch {
        tutupSetelahKonflikSelesai = true
        simpanDenganPemeriksaanKonflik(paksa = false)
    }
}

    override fun mintaTutup() {
        if (state.value.adaPerubahan) {
            _state.update {
                it.copy(konfirmasiTutup = true)
            }
        } else {
            onTutup()
        }
    }

    override fun tutupTanpaSimpan() {
        _state.update {
            it.copy(konfirmasiTutup = false)
        }

        onTutup()
    }

    override fun batalTutup() {
        _state.update {
            it.copy(konfirmasiTutup = false)
        }
    }

private suspend fun simpanInternal(): Boolean {
    val kondisi = state.value

    if (kondisi.hanyaBaca) {
        _state.update {
            it.copy(
                pesan = "Penyimpanan tidak tersedia karena lokasi hanya baca."
            )
        }

        return false
    }

    val teks = kondisi.medan.text

    _state.update {
        it.copy(
            sedangMenyimpan = true,
            pesan = null
        )
    }

    val hasil = dependensi.simpanTeks(
        lokasi,
        KontenTeks(
            teks = teks,
            encoding = kondisi.encoding ?: "UTF-8"
        )
    )

    _state.update {
        it.copy(
            sedangMenyimpan = false,
            pesan = hasil.pesan
        )
    }

    if (hasil.sukses) {
        _state.update {
            it.copy(teksAwal = teks)
        }
    }

    return hasil.sukses
}

    override fun ubahCari(nilai: String) {
        _state.update {
            it.copy(cari = nilai)
        }
    }

    override fun ubahGanti(nilai: String) {
        _state.update {
            it.copy(ganti = nilai)
        }
    }

override fun togglePekaHuruf() {
    val baru = !state.value.pekaHuruf

    _state.update {
        it.copy(pekaHuruf = baru)
    }

    simpanPreferensi(
        KunciPreferensiEditor.PEKA_HURUF,
        baru
    )
}

override fun toggleKataUtuh() {
    val baru = !state.value.kataUtuh

    _state.update {
        it.copy(kataUtuh = baru)
    }

    simpanPreferensi(
        KunciPreferensiEditor.KATA_UTUH,
        baru
    )
}

override fun toggleRegex() {
    val baru = !state.value.gunakanRegex

    _state.update {
        it.copy(gunakanRegex = baru)
    }

    simpanPreferensi(
        KunciPreferensiEditor.GUNAKAN_REGEX,
        baru
    )
}

override fun toggleBungkusKata() {
    val baru = !state.value.bungkusKata

    _state.update {
        it.copy(bungkusKata = baru)
    }

    simpanPreferensi(
        KunciPreferensiEditor.BUNGKUS_KATA,
        baru
    )
}

    override fun toggleKataUtuh() {
        _state.update {
            it.copy(kataUtuh = !it.kataUtuh)
        }
    }

    override fun toggleRegex() {
        _state.update {
            it.copy(gunakanRegex = !it.gunakanRegex)
        }
    }

    override fun toggleBungkusKata() {
        _state.update {
            it.copy(bungkusKata = !it.bungkusKata)
        }
    }

    override fun cariBerikutnya() {
        val kondisi = state.value
        val teks = kondisi.medan.text
        val kueri = kondisi.cari

        if (kueri.isEmpty()) return

        val mulai = kondisi.medan.selection.end

        if (kondisi.gunakanRegex) {
            val regex = buatRegex(kueri, kondisi.pekaHuruf)

            if (regex == null) {
                _state.update {
                    it.copy(
                        pesan = "Ekspresi reguler tidak valid."
                    )
                }

                return
            }

            val indeks = temukanRegexBerikutnya(
                regex = regex,
                teks = teks,
                mulai = mulai,
                kataUtuh = kondisi.kataUtuh
            )

            if (indeks >= 0) {
                val panjang = panjangMatchRegex(
                    regex = regex,
                    teks = teks,
                    indeks = indeks
                )

                val seleksi = TextRange(
                    indeks,
                    indeks + panjang
                )

                _state.update {
                    it.copy(
                        medan = TextFieldValue(
                            text = teks,
                            selection = seleksi
                        ),
                        pesan = "Kecocokan ditemukan."
                    )
                }

                tambahRiwayatCari(kueri)
                perbaruiPosisiKursor(state.value.medan)
            } else {
                _state.update {
                    it.copy(
                        pesan = "Tidak ditemukan."
                    )
                }
            }

            return
        }

        var indeks = temukanBerikutnya(
            teks = teks,
            kueri = kueri,
            mulai = mulai,
            pekaHuruf = kondisi.pekaHuruf,
            kataUtuh = kondisi.kataUtuh
        )

        if (indeks < 0) {
            indeks = temukanBerikutnya(
                teks = teks,
                kueri = kueri,
                mulai = 0,
                pekaHuruf = kondisi.pekaHuruf,
                kataUtuh = kondisi.kataUtuh
            )
        }

        if (indeks >= 0) {
            val seleksi = TextRange(
                indeks,
                indeks + kueri.length
            )

            _state.update {
                it.copy(
                    medan = TextFieldValue(
                        text = teks,
                        selection = seleksi
                    ),
                    pesan = "Kecocokan ditemukan."
                )
            }

            tambahRiwayatCari(kueri)
            perbaruiPosisiKursor(state.value.medan)
        } else {
            _state.update {
                it.copy(
                    pesan = "Tidak ditemukan."
                )
            }
        }
    }

    override fun cariSebelumnya() {
        val kondisi = state.value
        val teks = kondisi.medan.text
        val kueri = kondisi.cari

        if (kueri.isEmpty()) return

        val mulai = kondisi.medan.selection.start - 1

        if (kondisi.gunakanRegex) {
            val regex = buatRegex(kueri, kondisi.pekaHuruf)

            if (regex == null) {
                _state.update {
                    it.copy(
                        pesan = "Ekspresi reguler tidak valid."
                    )
                }

                return
            }

            val indeks = temukanRegexMundur(
                regex = regex,
                teks = teks,
                mulai = mulai,
                kataUtuh = kondisi.kataUtuh
            )

            if (indeks >= 0) {
                val panjang = panjangMatchRegex(
                    regex = regex,
                    teks = teks,
                    indeks = indeks
                )

                val seleksi = TextRange(
                    indeks,
                    indeks + panjang
                )

                _state.update {
                    it.copy(
                        medan = TextFieldValue(
                            text = teks,
                            selection = seleksi
                        ),
                        pesan = "Kecocokan ditemukan."
                    )
                }

                tambahRiwayatCari(kueri)
                perbaruiPosisiKursor(state.value.medan)
            } else {
                _state.update {
                    it.copy(
                        pesan = "Tidak ditemukan."
                    )
                }
            }

            return
        }

        var indeks = temukanMundur(
            teks = teks,
            kueri = kueri,
            mulai = mulai,
            pekaHuruf = kondisi.pekaHuruf,
            kataUtuh = kondisi.kataUtuh
        )

        if (indeks < 0) {
            indeks = temukanMundur(
                teks = teks,
                kueri = kueri,
                mulai = teks.length,
                pekaHuruf = kondisi.pekaHuruf,
                kataUtuh = kondisi.kataUtuh
            )
        }

        if (indeks >= 0) {
            val seleksi = TextRange(
                indeks,
                indeks + kueri.length
            )

            _state.update {
                it.copy(
                    medan = TextFieldValue(
                        text = teks,
                        selection = seleksi
                    ),
                    pesan = "Kecocokan ditemukan."
                )
            }

            tambahRiwayatCari(kueri)
            perbaruiPosisiKursor(state.value.medan)
        } else {
            _state.update {
                it.copy(
                    pesan = "Tidak ditemukan."
                )
            }
        }
    }

    override fun gantiSatu() {
        val kondisi = state.value
        val teks = kondisi.medan.text
        val kueri = kondisi.cari

        if (kondisi.hanyaBaca) return
        if (kueri.isEmpty()) return

        val seleksi = kondisi.medan.selection

        if (kondisi.gunakanRegex) {
            val regex = buatRegex(kueri, kondisi.pekaHuruf)

            if (regex == null) {
                _state.update {
                    it.copy(
                        pesan = "Ekspresi reguler tidak valid."
                    )
                }

                return
            }

            val posisi = if (seleksi.collapsed) {
                seleksi.start
            } else {
                seleksi.min
            }

            val match = regex.find(teks, posisi)

            if (
                match != null &&
                match.range.first == posisi &&
                (!kondisi.kataUtuh || cocokBatasKata(teks, posisi, match.value.length))
            ) {
                val teksBaru = teks.replaceRange(
                    match.range,
                    kondisi.ganti
                )

                val seleksiBaru = TextRange(
                    posisi + kondisi.ganti.length
                )

                terapkanTeksDenganRiwayat(teksBaru, seleksiBaru)
                tambahRiwayatCari(kueri)

                _state.update {
                    it.copy(
                        pesan = "Satu kecocokan diganti."
                    )
                }

                return
            }

            cariBerikutnya()

            return
        }

        if (!seleksi.collapsed && seleksi.length == kueri.length) {
            val cocok = cocokPadaPosisi(
                teks = teks,
                posisi = seleksi.min,
                kueri = kueri,
                pekaHuruf = kondisi.pekaHuruf,
                kataUtuh = kondisi.kataUtuh
            )

            if (cocok) {
                val teksBaru = teks.replaceRange(
                    seleksi.min,
                    seleksi.max,
                    kondisi.ganti
                )

                val seleksiBaru = TextRange(
                    seleksi.min + kondisi.ganti.length
                )

                terapkanTeksDenganRiwayat(teksBaru, seleksiBaru)
                tambahRiwayatCari(kueri)

                _state.update {
                    it.copy(
                        pesan = "Satu kecocokan diganti."
                    )
                }

                return
            }
        }

        cariBerikutnya()
    }

    override fun gantiSemua() {
        val kondisi = state.value
        val teks = kondisi.medan.text
        val kueri = kondisi.cari

        if (kondisi.hanyaBaca) return
        if (kueri.isEmpty()) return

        if (kondisi.gunakanRegex) {
            val regex = buatRegex(kueri, kondisi.pekaHuruf)

            if (regex == null) {
                _state.update {
                    it.copy(
                        pesan = "Ekspresi reguler tidak valid."
                    )
                }

                return
            }

            val hasil = gantiSemuaRegex(
                regex = regex,
                teks = teks,
                ganti = kondisi.ganti,
                kataUtuh = kondisi.kataUtuh
            )

            terapkanTeksDenganRiwayat(
                hasil.first,
                TextRange(0)
            )

            tambahRiwayatCari(kueri)

            _state.update {
                it.copy(
                    pesan = "${hasil.second} kecocokan diganti."
                )
            }

            return
        }

        val hasil = gantiSemuaTeks(
            teks = teks,
            cari = kueri,
            ganti = kondisi.ganti,
            pekaHuruf = kondisi.pekaHuruf,
            kataUtuh = kondisi.kataUtuh
        )

        terapkanTeksDenganRiwayat(
            hasil.first,
            TextRange(0)
        )

        tambahRiwayatCari(kueri)

        _state.update {
            it.copy(
                pesan = "${hasil.second} kecocokan diganti."
            )
        }
    }

    override fun bukaPanelStatistik() {
        _state.update {
            it.copy(panelStatistikTerbuka = true)
        }

        hitungStatistik()
    }

    override fun tutupPanelStatistik() {
        _state.update {
            it.copy(panelStatistikTerbuka = false)
        }
    }

override fun hitungStatistik() {
    val teks = state.value.medan.text
    val encoding = state.value.encoding

    scope.launch {
        _state.update {
            it.copy(sedangMenghitungStatistik = true)
        }

        val statistik = withContext(Dispatchers.Default) {
            dependensi.hitungStatistik(
                teks = teks,
                encoding = encoding
            )
        }

        _state.update {
            it.copy(
                sedangMenghitungStatistik = false,
                statistik = statistik
            )
        }
    }
}

    override fun bukaDialogNavigasiBaris() {
        _state.update {
            it.copy(
                dialogNavigasiBaris = true,
                nomorBarisInput = state.value.barisAktif.toString()
            )
        }
    }

    override fun tutupDialogNavigasiBaris() {
        _state.update {
            it.copy(
                dialogNavigasiBaris = false,
                nomorBarisInput = ""
            )
        }
    }

    override fun ubahNomorBarisInput(nilai: String) {
        _state.update {
            it.copy(nomorBarisInput = nilai)
        }
    }

    override fun konfirmasiNavigasiBaris() {
        val input = state.value.nomorBarisInput.trim()
        val nomor = input.toIntOrNull()
        val teks = state.value.medan.text

        if (nomor == null || nomor < 1) {
            _state.update {
                it.copy(
                    pesan = "Nomor baris tidak valid."
                )
            }

            return
        }

        val indeks = indeksAwalBaris(
            teks = teks,
            nomorBaris = nomor
        )

        if (indeks == null) {
            _state.update {
                it.copy(
                    pesan = "Nomor baris melebihi jumlah baris."
                )
            }

            return
        }

        val seleksi = TextRange(indeks)

        _state.update {
            it.copy(
                medan = TextFieldValue(
                    text = teks,
                    selection = seleksi
                ),
                dialogNavigasiBaris = false,
                nomorBarisInput = "",
                barisAktif = nomor,
                kolomAktif = 1,
                pesan = null
            )
        }
    }

    override fun bukaRiwayatCari() {
        _state.update {
            it.copy(dialogRiwayatCari = true)
        }
    }

    override fun tutupRiwayatCari() {
        _state.update {
            it.copy(dialogRiwayatCari = false)
        }
    }

    override fun pilihRiwayatCari(nilai: String) {
        _state.update {
            it.copy(
                cari = nilai,
                dialogRiwayatCari = false
            )
        }
    }

override fun hapusRiwayatCari() {
    _state.update {
        it.copy(riwayatCari = emptyList())
    }

    scope.launch {
        dependensi.hapusRiwayat()
    }
}

private fun tambahRiwayatCari(kueri: String) {
    if (kueri.isBlank()) return

    val sekarang = state.value.riwayatCari

    val sementara = listOf(kueri) + sekarang.filter { it != kueri }

    _state.update {
        it.copy(
            riwayatCari = sementara.take(20)
        )
    }

    scope.launch {
        dependensi.simpanRiwayat(kueri, 20)

        val daftar = dependensi.ambilRiwayat(20)

        _state.update {
            it.copy(riwayatCari = daftar)
        }
    }
}

    private fun perbaruiPosisiKursor(medan: TextFieldValue) {
        val teks = medan.text

        if (teks.length > BATAS_POSISI_KURSOR) {
            _state.update {
                it.copy(
                    barisAktif = 0,
                    kolomAktif = 0
                )
            }

            return
        }

        val posisi = hitungPosisiKursor(
            teks = teks,
            offset = medan.selection.end
        )

        _state.update {
            it.copy(
                barisAktif = posisi.first,
                kolomAktif = posisi.second
            )
        }
    }
}

private fun hitungPosisiKursor(
    teks: String,
    offset: Int
): Pair<Int, Int> {
    val batas = offset.coerceIn(0, teks.length)

    var baris = 1
    var awalBaris = 0
    var i = 0

    while (i < batas) {
        val karakter = teks[i]

        if (
            karakter == '\r' &&
            i + 1 < teks.length &&
            teks[i + 1] == '\n'
        ) {
            if (i + 2 <= batas) {
                baris++
                awalBaris = i + 2
                i += 2
                continue
            }

            i++
            continue
        }

        if (karakter == '\n' || karakter == '\r') {
            if (i + 1 <= batas) {
                baris++
                awalBaris = i + 1
            }

            i++
            continue
        }

        i++
    }

    val kolom = offset - awalBaris + 1

    return baris to kolom
}

private fun indeksAwalBaris(
    teks: String,
    nomorBaris: Int
): Int? {
    if (nomorBaris < 1) return null
    if (nomorBaris == 1) return 0

    var baris = 1
    var i = 0

    while (i < teks.length) {
        val karakter = teks[i]

        if (
            karakter == '\r' &&
            i + 1 < teks.length &&
            teks[i + 1] == '\n'
        ) {
            i += 2
            baris++

            if (baris == nomorBaris) return i

            continue
        }

        if (karakter == '\n' || karakter == '\r') {
            i++
            baris++

            if (baris == nomorBaris) return i

            continue
        }

        i++
    }

    if (baris + 1 == nomorBaris) {
        return teks.length
    }

    return null
}

private fun buatRegex(
    pola: String,
    pekaHuruf: Boolean
): Regex? {
    return try {
        val opsi = if (pekaHuruf) {
            emptySet()
        } else {
            setOf(RegexOption.IGNORE_CASE)
        }

        Regex(pola, opsi)
    } catch (e: Exception) {
        null
    }
}

private fun panjangMatchRegex(
    regex: Regex,
    teks: String,
    indeks: Int
): Int {
    val match = regex.find(teks, indeks)

    return if (
        match != null &&
        match.range.first == indeks
    ) {
        match.value.length
    } else {
        0
    }
}

private fun temukanRegexBerikutnya(
    regex: Regex,
    teks: String,
    mulai: Int,
    kataUtuh: Boolean
): Int {
    val dariMulai = cariRegex(
        regex = regex,
        teks = teks,
        mulai = mulai.coerceAtLeast(0),
        kataUtuh = kataUtuh,
        sampai = null
    )

    if (dariMulai >= 0) return dariMulai

    return cariRegex(
        regex = regex,
        teks = teks,
        mulai = 0,
        kataUtuh = kataUtuh,
        sampai = mulai.coerceAtLeast(0)
    )
}

private fun temukanRegexMundur(
    regex: Regex,
    teks: String,
    mulai: Int,
    kataUtuh: Boolean
): Int {
    val dariMundur = cariRegexMundur(
        regex = regex,
        teks = teks,
        batas = mulai.coerceAtLeast(0),
        kataUtuh = kataUtuh
    )

    if (dariMundur >= 0) return dariMundur

    return cariRegexMundur(
        regex = regex,
        teks = teks,
        batas = teks.length,
        kataUtuh = kataUtuh
    )
}

private fun cariRegex(
    regex: Regex,
    teks: String,
    mulai: Int,
    kataUtuh: Boolean,
    sampai: Int?
): Int {
    var posisi = mulai.coerceIn(0, teks.length)

    while (posisi <= teks.length) {
        val match = regex.find(teks, posisi) ?: break

        if (sampai != null && match.range.first >= sampai) break

        if (
            !kataUtuh ||
            cocokBatasKata(
                teks = teks,
                posisi = match.range.first,
                panjang = match.value.length
            )
        ) {
            return match.range.first
        }

        val berikutnya = match.range.first + 1

        if (berikutnya <= posisi) {
            posisi++
        } else {
            posisi = berikutnya
        }
    }

    return -1
}

private fun cariRegexMundur(
    regex: Regex,
    teks: String,
    batas: Int,
    kataUtuh: Boolean
): Int {
    var posisi = 0
    var terakhir = -1

    val batasAman = batas.coerceIn(0, teks.length)

    while (posisi <= teks.length) {
        val match = regex.find(teks, posisi) ?: break

        if (match.range.first > batasAman) break

        if (
            !kataUtuh ||
            cocokBatasKata(
                teks = teks,
                posisi = match.range.first,
                panjang = match.value.length
            )
        ) {
            terakhir = match.range.first
        }

        val berikutnya = match.range.first + 1

        if (berikutnya <= posisi) {
            posisi++
        } else {
            posisi = berikutnya
        }
    }

    return terakhir
}

private fun gantiSemuaRegex(
    regex: Regex,
    teks: String,
    ganti: String,
    kataUtuh: Boolean
): Pair<String, Int> {
    val hasil = StringBuilder()

    var indeks = 0
    var jumlah = 0

    while (true) {
        val ditemukan = cariRegex(
            regex = regex,
            teks = teks,
            mulai = indeks,
            kataUtuh = kataUtuh,
            sampai = null
        )

        if (ditemukan < 0) {
            hasil.appendRange(
                teks,
                indeks,
                teks.length
            )

            break
        }

        val match = regex.find(teks, ditemukan)

        if (match == null) {
            hasil.appendRange(
                teks,
                indeks,
                teks.length
            )

            break
        }

        hasil.appendRange(
            teks,
            indeks,
            ditemukan
        )

        hasil.append(ganti)

        val akhir = match.range.last + 1

        if (match.value.isEmpty()) {
            indeks = ditemukan + 1
        } else {
            indeks = akhir
        }

        jumlah++
    }

    return hasil.toString() to jumlah
}

private fun temukanBerikutnya(
    teks: String,
    kueri: String,
    mulai: Int,
    pekaHuruf: Boolean,
    kataUtuh: Boolean
): Int {
    if (kueri.isEmpty()) return -1

    if (!kataUtuh) {
        return teks.indexOf(
            string = kueri,
            startIndex = mulai.coerceAtLeast(0),
            ignoreCase = !pekaHuruf
        )
    }

    var posisi = mulai.coerceAtLeast(0)

    while (posisi <= teks.length - kueri.length) {
        val ditemukan = teks.indexOf(
            string = kueri,
            startIndex = posisi,
            ignoreCase = !pekaHuruf
        )

        if (ditemukan < 0) return -1

        if (cocokBatasKata(teks, ditemukan, kueri.length)) {
            return ditemukan
        }

        posisi = ditemukan + 1
    }

    return -1
}

private fun temukanMundur(
    teks: String,
    kueri: String,
    mulai: Int,
    pekaHuruf: Boolean,
    kataUtuh: Boolean
): Int {
    if (kueri.isEmpty()) return -1
    if (teks.length < kueri.length) return -1

    if (!kataUtuh) {
        return teks.lastIndexOf(
            string = kueri,
            startIndex = mulai.coerceAtMost(teks.length),
            ignoreCase = !pekaHuruf
        )
    }

    var posisi = mulai.coerceAtMost(teks.length - kueri.length)

    while (posisi >= 0) {
        val ditemukan = teks.lastIndexOf(
            string = kueri,
            startIndex = posisi,
            ignoreCase = !pekaHuruf
        )

        if (ditemukan < 0) return -1

        if (cocokBatasKata(teks, ditemukan, kueri.length)) {
            return ditemukan
        }

        posisi = ditemukan - 1
    }

    return -1
}

private fun cocokPadaPosisi(
    teks: String,
    posisi: Int,
    kueri: String,
    pekaHuruf: Boolean,
    kataUtuh: Boolean
): Boolean {
    if (posisi < 0) return false
    if (posisi + kueri.length > teks.length) return false

    val bagian = teks.substring(
        posisi,
        posisi + kueri.length
    )

    if (!bagian.equals(kueri, ignoreCase = !pekaHuruf)) {
        return false
    }

    if (kataUtuh) {
        return cocokBatasKata(
            teks = teks,
            posisi = posisi,
            panjang = kueri.length
        )
    }

    return true
}

private fun cocokBatasKata(
    teks: String,
    posisi: Int,
    panjang: Int
): Boolean {
    val sebelum = posisi - 1
    val sesudah = posisi + panjang

    if (sebelum >= 0) {
        val karakterSebelum = teks[sebelum]

        if (karakterKata(karakterSebelum)) {
            return false
        }
    }

    if (sesudah < teks.length) {
        val karakterSesudah = teks[sesudah]

        if (karakterKata(karakterSesudah)) {
            return false
        }
    }

    return true
}

private fun karakterKata(karakter: Char): Boolean {
    return karakter.isLetterOrDigit() || karakter == '_'
}

private fun gantiSemuaTeks(
    teks: String,
    cari: String,
    ganti: String,
    pekaHuruf: Boolean,
    kataUtuh: Boolean
): Pair<String, Int> {
    if (cari.isEmpty()) return teks to 0

    val hasil = StringBuilder()
    var indeks = 0
    var jumlah = 0

    while (true) {
        val ditemukan = temukanBerikutnya(
            teks = teks,
            kueri = cari,
            mulai = indeks,
            pekaHuruf = pekaHuruf,
            kataUtuh = kataUtuh
        )

        if (ditemukan < 0) {
            hasil.appendRange(
                teks,
                indeks,
                teks.length
            )

            break
        }

        hasil.appendRange(
            teks,
            indeks,
            ditemukan
        )

        hasil.append(ganti)

        indeks = ditemukan + cari.length
        jumlah++
    }

    return hasil.toString() to jumlah
}

private suspend fun simpanDenganPemeriksaanKonflik(
    paksa: Boolean
): Boolean {
    if (!paksa) {
        val awal = metadataAwal

        if (awal != null) {
            val kini = dependensi.dapatkanMetadata(lokasi)

            if (
                kini != null &&
                (
                    kini.waktuModifikasi != awal.waktuModifikasi ||
                        kini.ukuranByte != awal.ukuranByte
                    )
            ) {
                _state.update {
                    it.copy(konfirmasiKonflikEksternal = true)
                }

                return false
            }
        }
    }

    val sukses = simpanInternal()

    if (sukses) {
        metadataAwal = dependensi.dapatkanMetadata(lokasi)

        if (tutupSetelahKonflikSelesai) {
            _state.update {
                it.copy(konfirmasiKonflikEksternal = false)
            }

            onTutup()
        }
    }

    return sukses
}

override fun timpaKonflikEksternal() {
    scope.launch {
        _state.update {
            it.copy(konfirmasiKonflikEksternal = false)
        }

        simpanDenganPemeriksaanKonflik(paksa = true)
    }
}

override fun muatUlangKonflikEksternal() {
    _state.update {
        it.copy(
            konfirmasiKonflikEksternal = false,
            pesan = "Memuat ulang dari penyimpanan."
        )
    }

    tutupSetelahKonflikSelesai = false

    muatKonten()
}

override fun batalKonflikEksternal() {
    _state.update {
        it.copy(konfirmasiKonflikEksternal = false)
    }

    tutupSetelahKonflikSelesai = false
}

private fun muatPreferensi() {
    scope.launch {
        val bungkusKata = dependensi.ambilPreferensi(
            KunciPreferensiEditor.BUNGKUS_KATA,
            true
        )

        val pekaHuruf = dependensi.ambilPreferensi(
            KunciPreferensiEditor.PEKA_HURUF,
            false
        )

        val kataUtuh = dependensi.ambilPreferensi(
            KunciPreferensiEditor.KATA_UTUH,
            false
        )

        val gunakanRegex = dependensi.ambilPreferensi(
            KunciPreferensiEditor.GUNAKAN_REGEX,
            false
        )

        _state.update {
            it.copy(
                bungkusKata = bungkusKata,
                pekaHuruf = pekaHuruf,
                kataUtuh = kataUtuh,
                gunakanRegex = gunakanRegex
            )
        }
    }
}

private fun simpanPreferensi(
    kunci: String,
    nilai: Boolean
) {
    scope.launch {
        dependensi.simpanPreferensi(kunci, nilai)
    }
}

private fun muatRiwayat() {
    scope.launch {
        val daftar = dependensi.ambilRiwayat(20)

        _state.update {
            it.copy(riwayatCari = daftar)
        }
    }
}

