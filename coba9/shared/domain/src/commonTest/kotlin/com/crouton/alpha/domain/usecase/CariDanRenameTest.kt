package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.JenisEntri
import com.crouton.alpha.domain.model.LokasiBerkas
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CariDanRenameTest {
    private fun entri(nama: String, ukuran: Long = 100, jenis: JenisEntri = JenisEntri.BERKAS) =
        EntriBerkas(LokasiBerkas(nama, nama, nama, jenis == JenisEntri.FOLDER), ukuran, 0, jenis)

    @Test fun filterEkstensi() {
        val daftar = listOf(entri("a.txt"), entri("b.jpg"))
        assertEquals(1, CariBerkasUseCase.saring(daftar, "", FilterPencarian(ekstensi = "txt")).size)
    }

    @Test fun filterUkuran() {
        val daftar = listOf(entri("kecil.txt", 10), entri("besar.txt", 5000))
        val hasil = CariBerkasUseCase.saring(daftar, "", FilterPencarian(ukuranMinimum = 100))
        assertEquals(1, hasil.size)
        assertEquals("besar.txt", hasil[0].lokasi.namaTampil)
    }

    @Test fun pratinjauRenameKonflik() {
        val useCase = GantiNamaMassalUseCase()
        val hasil = useCase.buatPratinjau("{P}{S}", listOf("a.txt", "a.txt"), 1)
        assertTrue(hasil.all { !it.valid })
    }

    @Test fun pratinjauRenameValid() {
        val useCase = GantiNamaMassalUseCase()
        val hasil = useCase.buatPratinjau("file_{N}{S}", listOf("a.txt", "b.txt"), 1)
        assertTrue(hasil.all { it.valid })
        assertEquals("file_1.txt", hasil[0].namaBaru)
        assertEquals("file_2.txt", hasil[1].namaBaru)
    }
}
