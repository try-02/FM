package id.manajerberkas.kode.domain.usecase

import id.manajerberkas.kode.domain.kontrak.RepositoriArsip
import id.manajerberkas.kode.domain.model.HasilDaftarArsip
import id.manajerberkas.kode.domain.model.HasilEkstraksi
import id.manajerberkas.kode.domain.model.LokasiBerkas

class DaftarIsiArsipUseCase(
    private val repositori: RepositoriArsip
) {
    suspend operator fun invoke(lokasiArsip: LokasiBerkas): HasilDaftarArsip {
        return repositori.daftarIsi(lokasiArsip)
    }
}

class EkstrakItemArsipUseCase(
    private val repositori: RepositoriArsip
) {
    suspend operator fun invoke(
        lokasiArsip: LokasiBerkas,
        jalurItem: String,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi {
        return repositori.ekstrakItem(lokasiArsip, jalurItem, lokasiTujuan)
    }
}

class EkstrakSemuaArsipUseCase(
    private val repositori: RepositoriArsip
) {
    suspend operator fun invoke(
        lokasiArsip: LokasiBerkas,
        lokasiTujuan: LokasiBerkas
    ): HasilEkstraksi {
        return repositori.ekstrakSemua(lokasiArsip, lokasiTujuan)
    }
}