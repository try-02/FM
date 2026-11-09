package com.crouton.alpha.domain.util

object ValidasiNamaBerkas {
    private val karakterTerlarang = setOf('/', '\\', '\u0000')

    fun adalahNamaValid(nama: String): Boolean {
        if (nama.isBlank()) return false
        if (nama == "." || nama == "..") return false
        if (nama.any { it in karakterTerlarang }) return false
        return true
    }

    fun pesanKesalahan(nama: String): String = when {
        nama.isBlank() -> "Nama tidak boleh kosong"
        nama == "." || nama == ".." -> "Nama tidak boleh . atau .."
        nama.any { it in karakterTerlarang } -> "Nama mengandung karakter terlarang"
        else -> ""
    }
}
