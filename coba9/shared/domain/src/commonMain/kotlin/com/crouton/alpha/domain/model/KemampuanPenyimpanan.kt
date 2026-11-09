package com.crouton.alpha.domain.model

data class KemampuanPenyimpanan(
    val dapatGantiNama: Boolean,
    val dapatPindah: Boolean,
    val dapatSalin: Boolean,
    val dapatHapus: Boolean,
    val dapatTulis: Boolean,
    val dapatSymlink: Boolean = false,
    val dapatAtomicReplace: Boolean = false,
    val dapatUbahDiTempat: Boolean = false,
    val menyediakanPathPosix: Boolean = true
) {
    companion object {
        val Penuh = KemampuanPenyimpanan(true, true, true, true, true)
        val HanyaBaca = KemampuanPenyimpanan(false, false, false, false, false)
    }
}
