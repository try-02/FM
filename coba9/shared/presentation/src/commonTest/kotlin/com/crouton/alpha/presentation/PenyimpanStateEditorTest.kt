package com.crouton.alpha.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PenyimpanStateEditorTest {
    @Test fun menujuBarisValidDanInvalid() {
        val penyimpan = PenyimpanStateEditor()
        assertTrue(penyimpan.menujuBaris(5, 10))
        assertEquals(5, penyimpan.state.value.nomorBarisAktif)
        assertFalse(penyimpan.menujuBaris(99, 10))
        assertFalse(penyimpan.menujuBaris(0, 10))
    }

    @Test fun riwayatPencarian() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.tambahRiwayatPencarian("halo")
        penyimpan.tambahRiwayatPencarian("dunia")
        assertEquals(listOf("dunia", "halo"), penyimpan.state.value.riwayatPencarian)
        penyimpan.hapusRiwayatPencarian()
        assertTrue(penyimpan.state.value.riwayatPencarian.isEmpty())
    }

    @Test fun cariBerikutnyaMenemukanBaris() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.bukaBerkas("a.txt", "satu\ntwo\ntiga two")
        assertTrue(penyimpan.cariBerikutnya("two"))
        assertEquals(2, penyimpan.state.value.nomorBarisAktif)
    }

    @Test fun gantiSatuHanyaSatu() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.bukaBerkas("a.txt", "x x x")
        assertEquals(1, penyimpan.gantiSatu("x", "y"))
        assertEquals("y x x", penyimpan.state.value.teks)
    }

    @Test fun gantiSemuaMenghitung() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.bukaBerkas("a.txt", "x x x")
        assertEquals(3, penyimpan.gantiSemua("x", "y"))
        assertEquals("y y y", penyimpan.state.value.teks)
    }

    @Test fun ubahTeksMenandaiBerubah() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.ubahTeks("baru")
        assertEquals(StatusEditor.BERUBAH, penyimpan.state.value.status)
    }
}
