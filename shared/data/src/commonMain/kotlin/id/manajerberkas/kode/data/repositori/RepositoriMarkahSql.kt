package id.manajerberkas.kode.data.repositori

import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.data.lokal.Markah
import id.manajerberkas.kode.domain.kontrak.RepositoriMarkah
import id.manajerberkas.kode.domain.model.JenisMarkah
import id.manajerberkas.kode.domain.model.JenisPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MarkahLokasi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoriMarkahSql(
    private val database: DatabaseAplikasi
) : RepositoriMarkah {

    override suspend fun ambilSemua(): List<MarkahLokasi> =
        withContext(Dispatchers.Default) {
            database.markahQueries
                .pilihSemua()
                .executeAsList()
                .map { it.keDomain() }
        }

    override suspend fun simpan(markah: MarkahLokasi): Unit =
        withContext(Dispatchers.Default) {
            database.markahQueries.simpan(
                id_lokasi = markah.lokasi.id,
                nama_tampilan = markah.lokasi.namaTampilan,
                jalur_tampilan = markah.lokasi.jalurTampilan,
                jenis_penyimpanan = markah.lokasi.jenisPenyimpanan.name,
                jenis_markah = markah.jenis.name
            )
        }

    override suspend fun hapus(idLokasi: String): Unit =
        withContext(Dispatchers.Default) {
            database.markahQueries.hapus(idLokasi)
        }

    override suspend fun ada(idLokasi: String): Boolean =
        withContext(Dispatchers.Default) {
            database.markahQueries
                .ada(idLokasi)
                .executeAsOne() > 0
        }

    private fun Markah.keDomain(): MarkahLokasi {
        return MarkahLokasi(
            lokasi = LokasiBerkas(
                id = id_lokasi,
                namaTampilan = nama_tampilan,
                jalurTampilan = jalur_tampilan,
                jenisPenyimpanan = jenisPenyimpananDariDb(jenis_penyimpanan)
            ),
            jenis = jenisMarkahDariDb(jenis_markah)
        )
    }

    private fun jenisPenyimpananDariDb(nilai: String): JenisPenyimpanan {
        return JenisPenyimpanan.entries.firstOrNull { it.name == nilai }
            ?: JenisPenyimpanan.TIDAK_DIKENAL
    }

    private fun jenisMarkahDariDb(nilai: String): JenisMarkah {
        return JenisMarkah.entries.firstOrNull { it.name == nilai }
            ?: JenisMarkah.LOKASI_KERJA
    }
}