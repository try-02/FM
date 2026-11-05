package id.manajerberkas.kode.domain.kontrak
import id.manajerberkas.kode.domain.model.HasilBacaTeks
import id.manajerberkas.kode.domain.model.HasilDaftarBerkas
import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.HasilSimpanTeks
import id.manajerberkas.kode.domain.model.KemampuanPenyimpanan
import id.manajerberkas.kode.domain.model.KontenTeks
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MetadataBerkas
interface RepositoriBerkas {
    suspend fun daftarIsi(lokasi: LokasiBerkas): HasilDaftarBerkas
    suspend fun dapatkanKemampuan(lokasi: LokasiBerkas): KemampuanPenyimpanan
    suspend fun buatBerkas(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi
    suspend fun buatFolder(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi
    suspend fun gantiNama(
        lokasi: LokasiBerkas,
        namaBaru: String
    ): HasilOperasi
    suspend fun hapus(lokasi: LokasiBerkas): HasilOperasi
    suspend fun salinKe(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi
    suspend fun pindahkanKe(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi
    suspend fun bacaTeks(lokasi: LokasiBerkas): HasilBacaTeks
    suspend fun simpanTeks(
        lokasi: LokasiBerkas,
        konten: KontenTeks
    ): HasilSimpanTeks
    suspend fun dapatkanMetadata(lokasi: LokasiBerkas): MetadataBerkas?
}
