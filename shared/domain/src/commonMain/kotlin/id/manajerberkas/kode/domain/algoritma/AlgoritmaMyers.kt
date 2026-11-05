package id.manajerberkas.kode.domain.algoritma

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.CoroutineContext

internal object AlgoritmaMyers {
    // Batas aman untuk mencegah alokasi memori berlebih pada perangkat Android
    private const val BATAS_TOTAL_BARIS = 8_000
    private const val BATAS_KEDALAMAN_D = 1_200

    fun bandingkan(
        a: List<String>,
        b: List<String>,
        sama: (String, String) -> Boolean = { x, y -> x == y },
        konteks: CoroutineContext? = null
    ): List<Operasi> {
        if (a.isEmpty() && b.isEmpty()) return emptyList()
        if (a.isEmpty()) return b.map { Operasi.Tambah(it) }
        if (b.isEmpty()) return a.map { Operasi.Hapus(it) }

        val n = a.size
        val m = b.size
        if (n + m > BATAS_TOTAL_BARIS) {
            throw IllegalArgumentException("Jumlah total baris ($n + $m = ${n + m}) melebihi batas pembanding aman ($BATAS_TOTAL_BARIS baris).")
        }

        val max = n + m
        val batasD = minOf(max, BATAS_KEDALAMAN_D)
        val v = IntArray(2 * max + 1)
        val jejak = ArrayList<IntArray>(batasD + 1)
        var kedalamanAkhir = 0
        var selesai = false

        luar@ for (d in 0..batasD) {
            konteks?.ensureActive()
            jejak.add(v.copyOf())
            kedalamanAkhir = d
            for (k in -d..d step 2) {
                val indeks = k + max
                val x = if (k == -d || (k != d && v[indeks - 1] < v[indeks + 1])) {
                    v[indeks + 1]
                } else {
                    v[indeks - 1] + 1
                }
                var curX = x
                var curY = curX - k
                while (curX < n && curY < m && sama(a[curX], b[curY])) {
                    curX++
                    curY++
                }
                v[indeks] = curX
                if (curX >= n && curY >= m) {
                    selesai = true
                    break@luar
                }
            }
        }

        if (!selesai) {
            throw IllegalStateException("Perbedaan baris terlalu kompleks untuk dianalisis (melebihi batas kedalaman $BATAS_KEDALAMAN_D).")
        }

        return telusuriBalik(jejak, a, b, kedalamanAkhir, max)
    }

    private fun telusuriBalik(
        jejak: List<IntArray>,
        a: List<String>,
        b: List<String>,
        kedalamanAkhir: Int,
        max: Int
    ): List<Operasi> {
        var x = a.size
        var y = b.size
        val hasil = mutableListOf<Operasi>()
        for (d in kedalamanAkhir downTo 1) {
            val v = jejak[d]
            val k = x - y
            val indeksK = k + max
            val prevK = if (k == -d || (k != d && v[indeksK - 1] < v[indeksK + 1])) {
                k + 1
            } else {
                k - 1
            }
            val prevX = v[prevK + max]
            val prevY = prevX - prevK
            while (x > prevX && y > prevY) {
                x--
                y--
                hasil.add(Operasi.Sama(a[x]))
            }
            if (x == prevX) {
                y--
                hasil.add(Operasi.Tambah(b[y]))
            } else {
                x--
                hasil.add(Operasi.Hapus(a[x]))
            }
        }
        while (x > 0 && y > 0) {
            x--
            y--
            hasil.add(Operasi.Sama(a[x]))
        }
        hasil.reverse()
        return hasil
    }
}