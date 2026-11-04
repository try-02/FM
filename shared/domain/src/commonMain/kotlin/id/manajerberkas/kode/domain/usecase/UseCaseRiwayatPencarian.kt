package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriRiwayatPencarian

class AmbilRiwayatPencarianUseCase(
    private val repositori: RepositoriRiwayatPencarian
) {
    suspend operator fun invoke(batas: Int = 20): List<String> {
        return repositori.ambilSemua(batas)
    }
}

class SimpanRiwayatPencarianUseCase(
    private val repositori: RepositoriRiwayatPencarian
) {
    suspend operator fun invoke(
        nilai: String,
        batas: Int = 20
    ) {
        if (nilai.isBlank()) return

        repositori.simpan(nilai, batas)
    }
}

class HapusRiwayatPencarianUseCase(
    private val repositori: RepositoriRiwayatPencarian
) {
    suspend operator fun invoke() {
        repositori.hapusSemua()
    }
}