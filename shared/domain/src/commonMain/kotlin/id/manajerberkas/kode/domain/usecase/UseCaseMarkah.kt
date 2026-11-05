package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.kontrak.RepositoriMarkah
import id.manajerberkas.kode.domain.model.MarkahLokasi
class AmbilMarkahUseCase(
    private val repositori: RepositoriMarkah
) {
    suspend operator fun invoke(): List<MarkahLokasi> {
        return repositori.ambilSemua()
    }
}
class SimpanMarkahUseCase(
    private val repositori: RepositoriMarkah
) {
    suspend operator fun invoke(markah: MarkahLokasi) {
        repositori.simpan(markah)
    }
}
class HapusMarkahUseCase(
    private val repositori: RepositoriMarkah
) {
    suspend operator fun invoke(idLokasi: String) {
        repositori.hapus(idLokasi)
    }
}
class AdaMarkahUseCase(
    private val repositori: RepositoriMarkah
) {
    suspend operator fun invoke(idLokasi: String): Boolean {
        return repositori.ada(idLokasi)
    }
}
