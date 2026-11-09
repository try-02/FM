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

    @Test fun ubahTeksMenandaiBerubah() {
        val penyimpan = PenyimpanStateEditor()
        penyimpan.ubahTeks("baru")
        assertEquals(StatusEditor.BERUBAH, penyimpan.state.value.status)
    }
}
