package com.crouton.alpha.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenGantiNamaTest {
    @Test fun dasarDanEkstensi() {
        assertEquals("laporan", TokenGantiNama.namaDasar("laporan.txt"))
        assertEquals(".txt", TokenGantiNama.ekstensi("laporan.txt"))
        assertEquals("", TokenGantiNama.ekstensi("tanpaext"))
    }

    @Test fun polaNomorUrutan() {
        assertEquals("file_1.txt", TokenGantiNama.terapkanPola("file_{N}{S}", "a.txt", 1))
        assertEquals("file_2.txt", TokenGantiNama.terapkanPola("file_{N}{S}", "b.txt", 2))
    }

    @Test fun polaNolDiDepan() {
        assertEquals("gambar_08.jpg", TokenGantiNama.terapkanPola("gambar_{zN}.jpg", "a.jpg", 8))
        assertEquals("gambar_008.jpg", TokenGantiNama.terapkanPola("gambar_{z8:3}.jpg", "a.jpg", 8))
        assertEquals("gambar_100.jpg", TokenGantiNama.terapkanPola("gambar_{zN}.jpg", "a.jpg", 100))
    }

    @Test fun deteksiKonflik() {
        assertTrue(TokenGantiNama.deteksiKonflik(listOf("a.txt", "a.txt", "b.txt")).contains("a.txt"))
    }
}
