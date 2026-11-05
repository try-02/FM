package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.MetadataBerkas
class DapatkanMetadataBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(lokasi: LokasiBerkas): MetadataBerkas? {
        return repositoriBerkas.dapatkanMetadata(lokasi)
    }
}
