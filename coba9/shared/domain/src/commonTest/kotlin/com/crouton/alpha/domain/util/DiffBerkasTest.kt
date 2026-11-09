package com.crouton.alpha.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiffBerkasTest {
    @Test fun identik() {
        val hasil = DiffBerkas.bandingkan("a\nb", "a\nb")
        assertTrue(hasil.identik)
        assertEquals(0, hasil.jumlahPerubahan)
    }

    @Test fun tambahDanHapus() {
        val hasil = DiffBerkas.bandingkan("a\nb", "a\nc")
        assertEquals(2, hasil.jumlahPerubahan)
        assertEquals(1, hasil.baris.count { it.jenis == JenisPerbedaan.HAPUS })
        assertEquals(1, hasil.baris.count { it.jenis == JenisPerbedaan.TAMBAH })
    }

    @Test fun nomorBarisBenar() {
        val hasil = DiffBerkas.bandingkan("a\nb\nc", "a\nx\nc")
        val hapus = hasil.baris.first { it.jenis == JenisPerbedaan.HAPUS }
        val tambah = hasil.baris.first { it.jenis == JenisPerbedaan.TAMBAH }
        assertEquals(2, hapus.nomorBarisA)
        assertEquals(2, tambah.nomorBarisB)
    }

    @Test fun kosong() {
        val hasil = DiffBerkas.bandingkan("", "isi")
        assertEquals(1, hasil.jumlahPerubahan)
    }

    @Test fun abaikanSpasi() {
        val hasil = DiffBerkas.bandingkan("  a  ", "a", abaikanSpasi = true)
        assertTrue(hasil.identik)
    }

    @Test fun ekspor() {
        val hasil = DiffBerkas.bandingkan("a", "b")
        val teks = DiffBerkas.eksporTeks(hasil)
        assertTrue(teks.contains("- a"))
        assertTrue(teks.contains("+ b"))
    }
}
