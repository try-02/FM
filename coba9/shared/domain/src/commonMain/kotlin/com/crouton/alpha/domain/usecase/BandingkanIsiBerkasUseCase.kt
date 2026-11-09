package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.util.DiffBerkas
import com.crouton.alpha.domain.util.HasilDiff

class BandingkanIsiBerkasUseCase {
    fun jalankan(teksA: String, teksB: String, abaikanSpasi: Boolean = false, pekaHuruf: Boolean = true): HasilDiff {
        return DiffBerkas.bandingkan(teksA, teksB, abaikanSpasi, pekaHuruf)
    }
}
