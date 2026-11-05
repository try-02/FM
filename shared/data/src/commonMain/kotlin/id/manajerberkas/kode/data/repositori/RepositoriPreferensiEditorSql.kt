package id.manajerberkas.kode.data.repositori
import id.manajerberkas.kode.data.lokal.DatabaseAplikasi
import id.manajerberkas.kode.domain.kontrak.RepositoriPreferensiEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class RepositoriPreferensiEditorSql(
    private val database: DatabaseAplikasi
) : RepositoriPreferensiEditor {
    override suspend fun ambilBoolean(
        kunci: String,
        default: Boolean
    ): Boolean = withContext(Dispatchers.Default) {
        val nilai = database.preferensiEditorQueries
            .pilihNilai(kunci)
            .executeAsOneOrNull()
        when (nilai) {
            "true" -> true
            "false" -> false
            else -> default
        }
    }
    override suspend fun simpanBoolean(
        kunci: String,
        nilai: Boolean
    ): Unit = withContext(Dispatchers.Default) {
        database.preferensiEditorQueries.simpan(
            kunci,
            nilai.toString()
        )
    }
}
