package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.algoritma.AlgoritmaMyers
import id.manajerberkas.kode.domain.algoritma.Operasi
import id.manajerberkas.kode.domain.model.BarisDiff
import id.manajerberkas.kode.domain.model.HasilDiff
import id.manajerberkas.kode.domain.model.JenisOperasiDiff
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
class BandingkanIsiBerkasUseCase {
    suspend operator fun invoke(
        teksA: String,
        teksB: String,
        pekaHuruf: Boolean = true,
        abaikanSpasi: Boolean = false
    ): HasilDiff = withContext(Dispatchers.Default) {
        val barisA = pecahBaris(teksA)
        val barisB = pecahBaris(teksB)
        val pembanding = buatPembanding(pekaHuruf, abaikanSpasi)
        val operasi = AlgoritmaMyers.bandingkan(
            a = barisA,
            b = barisB,
            sama = pembanding,
            konteks = coroutineContext
        )
        val barisDiff = operasi.keBarisDiff()
        val jumlahPerubahan = barisDiff.count {
            it.jenis != JenisOperasiDiff.SAMA
        }
        HasilDiff(
            baris = barisDiff,
            jumlahPerubahan = jumlahPerubahan,
            identik = jumlahPerubahan == 0
        )
    }
    private fun pecahBaris(teks: String): List<String> {
        if (teks.isEmpty()) return emptyList()
        val dinormalisasi = teks
            .replace("\r\n", "\n")
            .replace('\r', '\n')
        val baris = dinormalisasi.split('\n')
        return if (dinormalisasi.endsWith("\n")) {
            baris.dropLast(1)
        } else {
            baris
        }
    }
    private fun buatPembanding(
        pekaHuruf: Boolean,
        abaikanSpasi: Boolean
    ): (String, String) -> Boolean {
        return { kiri, kanan ->
            val a = if (abaikanSpasi) kiri.trim() else kiri
            val b = if (abaikanSpasi) kanan.trim() else kanan
            if (pekaHuruf) {
                a == b
            } else {
                a.equals(b, ignoreCase = true)
            }
        }
    }
    private fun List<Operasi>.keBarisDiff(): List<BarisDiff> {
        var nomorA = 0
        var nomorB = 0
        return map { operasi ->
            when (operasi) {
                is Operasi.Sama -> {
                    nomorA++
                    nomorB++
                    BarisDiff(
                        jenis = JenisOperasiDiff.SAMA,
                        isi = operasi.isi,
                        nomorBarisA = nomorA,
                        nomorBarisB = nomorB
                    )
                }
                is Operasi.Hapus -> {
                    nomorA++
                    BarisDiff(
                        jenis = JenisOperasiDiff.HAPUS,
                        isi = operasi.isi,
                        nomorBarisA = nomorA,
                        nomorBarisB = null
                    )
                }
                is Operasi.Tambah -> {
                    nomorB++
                    BarisDiff(
                        jenis = JenisOperasiDiff.TAMBAH,
                        isi = operasi.isi,
                        nomorBarisA = null,
                        nomorBarisB = nomorB
                    )
                }
            }
        }
    }
}
