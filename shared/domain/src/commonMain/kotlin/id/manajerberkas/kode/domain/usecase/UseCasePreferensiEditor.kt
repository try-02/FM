package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.kontrak.RepositoriPreferensiEditor
class AmbilPreferensiEditorUseCase(
    private val repositori: RepositoriPreferensiEditor
) {
    suspend operator fun invoke(
        kunci: String,
        default: Boolean
    ): Boolean {
        return repositori.ambilBoolean(kunci, default)
    }
}
class SimpanPreferensiEditorUseCase(
    private val repositori: RepositoriPreferensiEditor
) {
    suspend operator fun invoke(
        kunci: String,
        nilai: Boolean
    ) {
        repositori.simpanBoolean(kunci, nilai)
    }
}
