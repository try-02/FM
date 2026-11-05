package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.model.JenisAkhirBaris
import id.manajerberkas.kode.domain.model.StatistikTeks
class HitungStatistikTeksUseCase {
    operator fun invoke(
        teks: String,
        encoding: String? = null
    ): StatistikTeks {
        val ukuranByte = teks.encodeToByteArray().size.toLong()
        val teksNormal = teks
            .replace("\r\n", "\n")
            .replace('\r', '\n')
        val baris = if (teksNormal.isEmpty()) {
            listOf("")
        } else {
            teksNormal.split('\n')
        }
        val jumlahBaris = baris.size
        val jumlahBarisKosong = baris.count { barisItem ->
            barisItem.trim().isEmpty()
        }
        val jumlahBarisTidakKosong = jumlahBaris - jumlahBarisKosong
        val jumlahKata = hitungKata(teks)
        val panjangBarisTerpanjang = baris.maxOfOrNull { barisItem ->
            barisItem.length
        } ?: 0
        val akhirBaris = deteksiAkhirBaris(teks)
        return StatistikTeks(
            ukuranByte = ukuranByte,
            encoding = encoding,
            akhirBaris = akhirBaris,
            jumlahBaris = jumlahBaris,
            jumlahBarisKosong = jumlahBarisKosong,
            jumlahBarisTidakKosong = jumlahBarisTidakKosong,
            jumlahKata = jumlahKata,
            jumlahKarakter = teks.length,
            panjangBarisTerpanjang = panjangBarisTerpanjang,
            jumlahBarisKode = jumlahBarisTidakKosong
        )
    }
    private fun hitungKata(teks: String): Int {
        var jumlah = 0
        var diDalamKata = false
        for (karakter in teks) {
            if (karakter.isWhitespace()) {
                diDalamKata = false
            } else {
                if (!diDalamKata) {
                    jumlah++
                    diDalamKata = true
                }
            }
        }
        return jumlah
    }
    private fun deteksiAkhirBaris(teks: String): JenisAkhirBaris? {
        var crlf = 0
        var lf = 0
        var cr = 0
        var i = 0
        while (i < teks.length) {
            val karakter = teks[i]
            if (karakter == '\r') {
                if (i + 1 < teks.length && teks[i + 1] == '\n') {
                    crlf++
                    i += 2
                    continue
                }
                cr++
                i++
                continue
            }
            if (karakter == '\n') {
                lf++
            }
            i++
        }
        return when {
            crlf == 0 && lf == 0 && cr == 0 -> null
            crlf > 0 && lf == 0 && cr == 0 -> JenisAkhirBaris.CRLF
            lf > 0 && crlf == 0 && cr == 0 -> JenisAkhirBaris.LF
            cr > 0 && crlf == 0 && lf == 0 -> JenisAkhirBaris.CR
            else -> JenisAkhirBaris.CAMPURAN
        }
    }
}
