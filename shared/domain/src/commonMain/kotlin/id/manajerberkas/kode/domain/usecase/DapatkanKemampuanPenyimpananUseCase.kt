package id.manajerberkas.kode.domain.usecase
import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.KemampuanPenyimpanan
import id.manajerberkas.kode.domain.model.LokasiBerkas
class DapatkanKemampuanPenyimpananUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(lokasi: LokasiBerkas): KemampuanPenyimpanan {
        return repositoriBerkas.dapatkanKemampuan(lokasi)
    }
}
