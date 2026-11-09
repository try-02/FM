package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.model.HasilSimpanBerkas
import com.crouton.alpha.domain.model.KontenBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.model.StatusSimpan
import com.crouton.alpha.domain.repository.RepositoriBerkas

class SimpanPerubahanUseCase(private val repositori: RepositoriBerkas) {
    suspend fun jalankan(lokasi: LokasiBerkas, konten: KontenBerkas, versiDibuka: Long): HasilSimpanBerkas {
        val versiSekarang = repositori.versiEksternal(lokasi)
        if (versiSekarang != versiDibuka) {
            return HasilSimpanBerkas(StatusSimpan.KONFLIK_EKSTERNAL, "File berubah di luar aplikasi")
        }
        val kemampuan = repositori.kemampuan(lokasi)
        if (!kemampuan.dapatTulis) {
            return HasilSimpanBerkas(StatusSimpan.HANYA_BACA, "Penyimpanan tidak dapat ditulis")
        }
        return repositori.simpan(lokasi, konten)
    }
}
