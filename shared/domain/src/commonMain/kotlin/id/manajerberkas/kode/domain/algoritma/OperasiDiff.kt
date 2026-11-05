package id.manajerberkas.kode.domain.algoritma
internal sealed class Operasi {
    data class Sama(val isi: String) : Operasi()
    data class Hapus(val isi: String) : Operasi()
    data class Tambah(val isi: String) : Operasi()
}
