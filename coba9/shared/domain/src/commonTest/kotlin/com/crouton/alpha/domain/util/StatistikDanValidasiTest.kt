package com.crouton.alpha.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StatistikDanValidasiTest {
    @Test fun statistikDasar() {
        val hasil = StatistikTeks.hitung("a\n\nb", 4)
        assertEquals(3, hasil.jumlahBaris)
        assertEquals(1, hasil.barisKosong)
        assertEquals(2, hasil.barisTidakKosong)
        assertEquals(2, hasil.jumlahKata)
    }

    @Test fun statistikCrlf() {
        val hasil = StatistikTeks.hitung("a\r\nb", 4)
        assertEquals("CRLF", hasil.akhirBaris)
    }

    @Test fun validasiNama() {
        assertTrue(ValidasiNamaBerkas.adalahNamaValid("file.txt"))
        assertFalse(ValidasiNamaBerkas.adalahNamaValid(""))
        assertFalse(ValidasiNamaBerkas.adalahNamaValid("a/b"))
        assertFalse(ValidasiNamaBerkas.adalahNamaValid(".."))
    }
}
