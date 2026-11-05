package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.model.JenisOperasiDiff
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
class BandingkanIsiBerkasUseCaseTest {
    private val useCase = BandingkanIsiBerkasUseCase()
    @Test
    fun fileIdentikTidakMenghasilkanPerubahan() = runTest {
        val hasil = useCase(
            teksA = "baris satu\nbaris dua",
            teksB = "baris satu\nbaris dua"
        )
        assertTrue(hasil.identik)
        assertEquals(0, hasil.jumlahPerubahan)
    }
    @Test
    fun fileKosongDibandingkanDenganFileKosong() = runTest {
        val hasil = useCase(
            teksA = "",
            teksB = ""
        )
        assertTrue(hasil.identik)
        assertEquals(0, hasil.jumlahPerubahan)
    }
    @Test
    fun semuaBarisDitambahkanJikaFileAKosong() = runTest {
        val hasil = useCase(
            teksA = "",
            teksB = "satu\ndua"
        )
        assertEquals(2, hasil.jumlahPerubahan)
        assertTrue(hasil.baris.all { it.jenis == JenisOperasiDiff.TAMBAH })
    }
    @Test
    fun semuaBarisDihapusJikaFileBKosong() = runTest {
        val hasil = useCase(
            teksA = "satu\ndua",
            teksB = ""
        )
        assertEquals(2, hasil.jumlahPerubahan)
        assertTrue(hasil.baris.all { it.jenis == JenisOperasiDiff.HAPUS })
    }
    @Test
    fun perubahanSatuBarisMenghasilkanHapusDanTambah() = runTest {
        val hasil = useCase(
            teksA = "return harga",
            teksB = "return harga + pajak"
        )
        assertEquals(2, hasil.jumlahPerubahan)
        val jenis = hasil.baris.map { it.jenis }
        assertTrue(jenis.contains(JenisOperasiDiff.HAPUS))
        assertTrue(jenis.contains(JenisOperasiDiff.TAMBAH))
    }
    @Test
    fun nomorBarisKeduaSisiTetapBenar() = runTest {
        val hasil = useCase(
            teksA = "a\nb\nc",
            teksB = "a\nx\nc"
        )
        val barisA = hasil.baris.filter { it.nomorBarisA != null }
        val barisB = hasil.baris.filter { it.nomorBarisB != null }
        assertEquals(listOf(1, 2, 3), barisA.map { it.nomorBarisA })
        assertEquals(listOf(1, 2, 3), barisB.map { it.nomorBarisB })
    }
    @Test
    fun opsiPekaHurufMempengaruhiHasil() = runTest {
        val hasilPeka = useCase(
            teksA = "ABC",
            teksB = "abc",
            pekaHuruf = true
        )
        val hasilTidakPeka = useCase(
            teksA = "ABC",
            teksB = "abc",
            pekaHuruf = false
        )
        assertTrue(hasilPeka.jumlahPerubahan > 0)
        assertTrue(hasilTidakPeka.identik)
    }
    @Test
    fun opsiAbaikanSpasiMempengaruhiHasil() = runTest {
        val hasilDenganSpasi = useCase(
            teksA = "  nilai = 1",
            teksB = "nilai = 1",
            abaikanSpasi = false
        )
        val hasilAbaikanSpasi = useCase(
            teksA = "  nilai = 1",
            teksB = "nilai = 1",
            abaikanSpasi = true
        )
        assertTrue(hasilDenganSpasi.jumlahPerubahan > 0)
        assertTrue(hasilAbaikanSpasi.identik)
    }
    @Test
    fun akhirBarisCrlfDanLfDianggapSamaUntukIsiBaris() = runTest {
        val hasil = useCase(
            teksA = "satu\r\ndua",
            teksB = "satu\ndua"
        )
        assertTrue(hasil.identik)
    }
}
