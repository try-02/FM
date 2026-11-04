package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriBerkas
import id.manajerberkas.kode.domain.model.HasilOperasi
import id.manajerberkas.kode.domain.model.LokasiBerkas
import id.manajerberkas.kode.domain.model.ValidasiNamaBerkas

class BuatBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi {
        val validasi = ValidasiNamaBerkas.periksa(nama)

        if (!validasi.sukses) {
            return HasilOperasi(
                sukses = false,
                pesan = validasi.pesan
            )
        }

        return repositoriBerkas.buatBerkas(lokasiInduk, nama)
    }
}

class BuatFolderUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        lokasiInduk: LokasiBerkas,
        nama: String
    ): HasilOperasi {
        val validasi = ValidasiNamaBerkas.periksa(nama)

        if (!validasi.sukses) {
            return HasilOperasi(
                sukses = false,
                pesan = validasi.pesan
            )
        }

        return repositoriBerkas.buatFolder(lokasiInduk, nama)
    }
}

class GantiNamaBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        lokasi: LokasiBerkas,
        namaBaru: String
    ): HasilOperasi {
        val validasi = ValidasiNamaBerkas.periksa(namaBaru)

        if (!validasi.sukses) {
            return HasilOperasi(
                sukses = false,
                pesan = validasi.pesan
            )
        }

        return repositoriBerkas.gantiNama(lokasi, namaBaru)
    }
}

class HapusBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(lokasi: LokasiBerkas): HasilOperasi {
        return repositoriBerkas.hapus(lokasi)
    }
}

class SalinBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi {
        return repositoriBerkas.salinKe(sumber, lokasiIndukTujuan)
    }
}

class PindahkanBerkasUseCase(
    private val repositoriBerkas: RepositoriBerkas
) {
    suspend operator fun invoke(
        sumber: LokasiBerkas,
        lokasiIndukTujuan: LokasiBerkas
    ): HasilOperasi {
        return repositoriBerkas.pindahkanKe(sumber, lokasiIndukTujuan)
    }
}