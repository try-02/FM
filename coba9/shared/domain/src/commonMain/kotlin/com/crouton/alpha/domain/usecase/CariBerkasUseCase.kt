package com.crouton.alpha.domain.usecase

import com.crouton.alpha.domain.model.EntriBerkas
import com.crouton.alpha.domain.model.JenisEntri

data class FilterPencarian(
    val ekstensi: String? = null,
    val ukuranMinimum: Long? = null,
    val ukuranMaksimum: Long? = null,
    val hanyaFolder: Boolean = false,
    val hanyaBerkas: Boolean = false,
    val pekaHuruf: Boolean = false
)

object CariBerkasUseCase {
    fun cocokkan(entri: EntriBerkas, kataKunci: String, filter: FilterPencarian): Boolean {
        val nama = entri.lokasi.namaTampil
        val namaDibandingkan = if (filter.pekaHuruf) nama else nama.lowercase()
        val kunciDibandingkan = if (filter.pekaHuruf) kataKunci else kataKunci.lowercase()
        if (!namaDibandingkan.contains(kunciDibandingkan)) return false
        if (filter.ekstensi != null) {
            val ext = filter.ekstensi.lowercase().removePrefix(".")
            if (!nama.lowercase().endsWith(".$ext")) return false
        }
        if (filter.ukuranMinimum != null && entri.ukuranByte < filter.ukuranMinimum) return false
        if (filter.ukuranMaksimum != null && entri.ukuranByte > filter.ukuranMaksimum) return false
        if (filter.hanyaFolder && entri.jenis != JenisEntri.FOLDER) return false
        if (filter.hanyaBerkas && entri.jenis == JenisEntri.FOLDER) return false
        return true
    }

    fun saring(daftar: List<EntriBerkas>, kataKunci: String, filter: FilterPencarian): List<EntriBerkas> {
        return daftar.filter { cocokkan(it, kataKunci, filter) }
    }
}
