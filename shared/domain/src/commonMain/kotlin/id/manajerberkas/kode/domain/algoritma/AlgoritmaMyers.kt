package id.manajerberkas.kode.domain.algoritma

import kotlinx.coroutines.ensureActive
import kotlin.coroutines.CoroutineContext

internal object AlgoritmaMyers {

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
        val max = n + m

        val v = IntArray(2 * max + 1)
        val jejak = mutableListOf<IntArray>()

        var kedalamanAkhir = 0

        luar@ for (d in 0..max) {
            konteks?.ensureActive()

            jejak.add(v.copyOf())
            kedalamanAkhir = d

            for (k in -d..d step 2) {
                val indeks = k + max

                var x: Int

                if (k == -d || (k != d && v[indeks - 1] < v[indeks + 1])) {
                    x = v[indeks + 1]
                } else {
                    x = v[indeks - 1] + 1
                }

                var y = x - k

                while (x < n && y < m && sama(a[x], b[y])) {
                    x++
                    y++
                }

                v[indeks] = x

                if (x >= n && y >= m) {
                    break@luar
                }
            }
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