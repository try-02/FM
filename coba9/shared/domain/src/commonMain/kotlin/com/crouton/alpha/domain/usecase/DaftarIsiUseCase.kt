package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.model.HasilDaftarBerkas
import com.crouton.alpha.domain.model.LokasiBerkas
import com.crouton.alpha.domain.repository.RepositoriBerkas

class DaftarIsiUseCase(private val repositori: RepositoriBerkas) {
    suspend fun jalankan(lokasi: LokasiBerkas): HasilDaftarBerkas = repositori.daftarIsi(lokasi)
}
