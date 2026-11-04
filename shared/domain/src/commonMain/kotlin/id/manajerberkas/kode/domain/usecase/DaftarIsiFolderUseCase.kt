package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.HasilDaftarBerkas
import id.manajerberkas.kode.domain.model.LokasiBerkas

class DaftarIsiFolderUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(lokasi: LokasiBerkas): HasilDaftarBerkas {
        return repositoriBerkas.daftarIsi(lokasi)
    }
}