package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriBeranda
import id.manajerberkas.kode.domain.model.LokasiBerkas

class AmbilBerandaUseCase(
    private val repositori: RepositoriBeranda
) {
    suspend operator fun invoke(): LokasiBerkas? {
        return repositori.ambil()
    }
}

class SimpanBerandaUseCase(
    private val repositori: RepositoriBeranda
) {
    suspend operator fun invoke(lokasi: LokasiBerkas) {
        repositori.simpan(lokasi)
    }
}

class HapusBerandaUseCase(
    private val repositori: RepositoriBeranda
) {
    suspend operator fun invoke() {
        repositori.hapus()
    }
}