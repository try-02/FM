package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.HasilBacaTeks
import id.manajerberkas.kode.domain.model.HasilSimpanTeks
import id.manajerberkas.kode.domain.model.KontenTeks
import id.manajerberkas.kode.domain.model.LokasiBerkas

class BacaTeksUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(lokasi: LokasiBerkas): HasilBacaTeks {
        return repositoriBerkas.bacaTeks(lokasi)
    }
}

class SimpanTeksUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        lokasi: LokasiBerkas,
        konten: KontenTeks
    ): HasilSimpanTeks {
        return repositoriBerkas.simpanTeks(lokasi, konten)
    }
}