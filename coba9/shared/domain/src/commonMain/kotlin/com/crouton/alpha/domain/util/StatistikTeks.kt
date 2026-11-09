package com.crouton.alpha.domain.util

data class HasilStatistikTeks(
    val ukuranByte: Long,
    val jumlahBaris: Int,
    val barisKosong: Int,
    val barisTidakKosong: Int,
    val jumlahKata: Int,
    val jumlahKarakter: Int,
    val barisTerpanjang: Int,
    val akhirBaris: String,
    val encoding: String,
    val barisKode: Int
)

object StatistikTeks {
    fun hitung(teks: String, ukuranByte: Long, encoding: String = "UTF-8"): HasilStatistikTeks {
        if (teks.isEmpty()) {
            return HasilStatistikTeks(ukuranByte, 0, 0, 0, 0, 0, 0, "LF", encoding, 0)
        }
        val akhirBaris = if (teks.contains("\r\n")) "CRLF" else "LF"
        val baris = teks.split("\n")
        val kosong = baris.count { it.trim().isEmpty() }
        val kata = teks.split(Regex("\\s+")).count { it.isNotEmpty() }
        val terpanjang = baris.maxOfOrNull { it.length } ?: 0
        val kode = baris.count { barisTeks -> barisTeks.trim().isNotEmpty() }
        return HasilStatistikTeks(
            ukuranByte, baris.size, kosong, baris.size - kosong,
            kata, teks.length, terpanjang, akhirBaris, encoding, kode
        )
    }
}
