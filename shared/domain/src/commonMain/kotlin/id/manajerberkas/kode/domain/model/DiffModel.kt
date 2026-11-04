package id.manajerberkas.kode.domain.model

enum class JenisOperasiDiff {
    SAMA,
    HAPUS,
    TAMBAH
}

enum class ModeDiff {
    UNIFIED,
    BERDAMPINGAN
}

data class BarisDiff(
    val jenis: JenisOperasiDiff,
    val isi: String,
    val nomorBarisA: Int?,
    val nomorBarisB: Int?
)

data class HasilDiff(
    val baris: List<BarisDiff>,
    val jumlahPerubahan: Int,
    val identik: Boolean
)

data class BarisDiffBerdampingan(
    val teksA: String?,
    val nomorA: Int?,
    val jenisA: JenisOperasiDiff?, // HAPUS atau SAMA. null berarti penyeimbang (padding)
    
    val teksB: String?,
    val nomorB: Int?,
    val jenisB: JenisOperasiDiff?, // TAMBAH atau SAMA. null berarti penyeimbang (padding)
    
    val blokPerubahan: Boolean
)

data class BlokPerubahan(
    val indeksAwal: Int,
    val indeksAkhir: Int
)

fun HasilDiff.keBerdampingan(): List<BarisDiffBerdampingan> {
    val hasil = mutableListOf<BarisDiffBerdampingan>()
    var i = 0
    while (i < baris.size) {
        val current = baris[i]
        if (current.jenis == JenisOperasiDiff.SAMA) {
            hasil.add(
                BarisDiffBerdampingan(
                    teksA = current.isi, nomorA = current.nomorBarisA, jenisA = JenisOperasiDiff.SAMA,
                    teksB = current.isi, nomorB = current.nomorBarisB, jenisB = JenisOperasiDiff.SAMA,
                    blokPerubahan = false
                )
            )
            i++
        } else {
            val hapus = mutableListOf<BarisDiff>()
            val tambah = mutableListOf<BarisDiff>()
            while (i < baris.size && baris[i].jenis != JenisOperasiDiff.SAMA) {
                if (baris[i].jenis == JenisOperasiDiff.HAPUS) hapus.add(baris[i])
                else if (baris[i].jenis == JenisOperasiDiff.TAMBAH) tambah.add(baris[i])
                i++
            }
            val maxRows = maxOf(hapus.size, tambah.size)
            for (j in 0 until maxRows) {
                val h = hapus.getOrNull(j)
                val t = tambah.getOrNull(j)
                hasil.add(
                    BarisDiffBerdampingan(
                        teksA = h?.isi, nomorA = h?.nomorBarisA, jenisA = if (h != null) JenisOperasiDiff.HAPUS else null,
                        teksB = t?.isi, nomorB = t?.nomorBarisB, jenisB = if (t != null) JenisOperasiDiff.TAMBAH else null,
                        blokPerubahan = true
                    )
                )
            }
        }
    }
    return hasil
}

fun List<BarisDiff>.ekstrakBlokUnified(): List<BlokPerubahan> {
    val blok = mutableListOf<BlokPerubahan>()
    var start = -1
    for (i in indices) {
        if (this[i].jenis != JenisOperasiDiff.SAMA) {
            if (start == -1) start = i
        } else {
            if (start != -1) { blok.add(BlokPerubahan(start, i - 1)); start = -1 }
        }
    }
    if (start != -1) blok.add(BlokPerubahan(start, lastIndex))
    return blok
}

fun List<BarisDiffBerdampingan>.ekstrakBlokBerdampingan(): List<BlokPerubahan> {
    val blok = mutableListOf<BlokPerubahan>()
    var start = -1
    for (i in indices) {
        if (this[i].blokPerubahan) {
            if (start == -1) start = i
        } else {
            if (start != -1) { blok.add(BlokPerubahan(start, i - 1)); start = -1 }
        }
    }
    if (start != -1) blok.add(BlokPerubahan(start, lastIndex))
    return blok
}