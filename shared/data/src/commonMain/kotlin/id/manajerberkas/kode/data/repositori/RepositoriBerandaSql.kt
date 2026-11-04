package id.manajerberkas.kode.data.repositori

import id.manajerberkas.kode.data.lokal.Beranda
import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.domain.kontrak.RepositoriBeranda
import id.manajerberkas.kode.domain.model.JenisPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RepositoriBerandaSql(
    private val database: DatabaseAplikasi
) : RepositoriBeranda {

    override suspend fun ambil(): LokasiBerkas? =
        withContext(Dispatchers.Default) {
            database.berandaQueries
                .ambil()
                .executeAsOneOrNull()
                ?.keDomain()
        }

    override suspend fun simpan(lokasi: LokasiBerkas): Unit =
        withContext(Dispatchers.Default) {
            database.berandaQueries.simpan(
                id_lokasi = lokasi.id,
                nama_tampilan = lokasi.namaTampilan,
                jalur_tampilan = lokasi.jalurTampilan,
                jenis_penyimpanan = lokasi.jenisPenyimpanan.name
            )
        }

    override suspend fun hapus(): Unit =
        withContext(Dispatchers.Default) {
            database.berandaQueries.hapus()
        }

    private fun Beranda.keDomain(): LokasiBerkas {
        return LokasiBerkas(
            id = id_lokasi,
            namaTampilan = nama_tampilan,
            jalurTampilan = jalur_tampilan,
            jenisPenyimpanan = jenisPenyimpananDariDb(jenis_penyimpanan)
        )
    }

    private fun jenisPenyimpananDariDb(nilai: String): JenisPenyimpanan {
        return JenisPenyimpanan.entries.firstOrNull { it.name == nilai }
            ?: JenisPenyimpanan.TIDAK_DIKENAL
    }
}