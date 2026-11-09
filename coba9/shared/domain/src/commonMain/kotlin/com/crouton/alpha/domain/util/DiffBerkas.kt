package com.crouton.alpha.domain.util

enum class JenisPerbedaan { SAMA, TAMBAH, HAPUS }

data class BarisDiff(
    val jenis: JenisPerbedaan,
    val teks: String,
    val nomorBarisA: Int?,
    val nomorBarisB: Int?
)

data class HasilDiff(
    val baris: List<BarisDiff>,
    val jumlahPerubahan: Int,
    val identik: Boolean
)

// Algoritme Myers O(ND) dengan trace untuk backtrack (PRD 8.5).
object DiffBerkas {
    fun bandingkan(
        teksA: String,
        teksB: String,
        abaikanSpasi: Boolean = false,
        pekaHuruf: Boolean = true
    ): HasilDiff {
        fun normalisasi(baris: String): String {
            var hasil = baris
            if (abaikanSpasi) hasil = hasil.trim()
            if (!pekaHuruf) hasil = hasil.lowercase()
            return hasil
        }
        val a = if (teksA.isEmpty()) emptyList() else teksA.split("\n").map { normalisasi(it) }
        val b = if (teksB.isEmpty()) emptyList() else teksB.split("\n").map { normalisasi(it) }
        val teksMentahA = if (teksA.isEmpty()) emptyList() else teksA.split("\n")
        val teksMentahB = if (teksB.isEmpty()) emptyList() else teksB.split("\n")
        val n = a.size
        val m = b.size
        val maks = n + m
        if (maks == 0) return HasilDiff(emptyList(), 0, true)
        val v = IntArray(2 * maks + 1)
        val lintasan = mutableListOf<IntArray>()
        var dFinal = -1
        luar@ for (d in 0..maks) {
            for (k in -d..d step 2) {
                var x = if (k == -d || (k != d && v[k - 1 + maks] < v[k + 1 + maks])) {
                    v[k + 1 + maks]
                } else {
                    v[k - 1 + maks] + 1
                }
                var y = x - k
                while (x < n && y < m && a[x] == b[y]) { x++; y++ }
                v[k + maks] = x
                if (x >= n && y >= m) { dFinal = d; break@luar }
            }
            lintasan.add(v.copyOf())
        }
        // Backtrack
        val hasil = mutableListOf<BarisDiff>()
        var x = n
        var y = m
        for (d in dFinal downTo 1) {
            val vv = lintasan[d - 1]
            val k = x - y
            val kSebelumnya = if (k == -d || (k != d && vv[k - 1 + maks] < vv[k + 1 + maks])) {
                k + 1
            } else {
                k - 1
            }
            val xSebelumnya = vv[kSebelumnya + maks]
            val ySebelumnya = xSebelumnya - kSebelumnya
            while (x > xSebelumnya && y > ySebelumnya) {
                hasil.add(BarisDiff(JenisPerbedaan.SAMA, teksMentahA[x - 1], x, y))
                x--; y--
            }
            if (x == xSebelumnya) {
                hasil.add(BarisDiff(JenisPerbedaan.TAMBAH, teksMentahB[y - 1], null, y))
                y--
            } else {
                hasil.add(BarisDiff(JenisPerbedaan.HAPUS, teksMentahA[x - 1], x, null))
                x--
            }
        }
        while (x > 0 && y > 0) {
            hasil.add(BarisDiff(JenisPerbedaan.SAMA, teksMentahA[x - 1], x, y))
            x--; y--
        }
        hasil.reverse()
        val perubahan = hasil.count { it.jenis != JenisPerbedaan.SAMA }
        return HasilDiff(hasil, perubahan, perubahan == 0)
    }

    fun eksporTeks(hasil: HasilDiff): String {
        return hasil.baris.joinToString("\n") { baris ->
            when (baris.jenis) {
                JenisPerbedaan.TAMBAH -> "+ " + baris.teks
                JenisPerbedaan.HAPUS -> "- " + baris.teks
                JenisPerbedaan.SAMA -> "  " + baris.teks
            }
        }
    }
}
