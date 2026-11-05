package id.manajerberkas.kode.domain.model
enum class JenisAkhirBaris {
    LF,
    CRLF,
    CR,
    CAMPURAN
}
data class StatistikTeks(
    val ukuranByte: Long?,
    val encoding: String?,
    val akhirBaris: JenisAkhirBaris?,
    val jumlahBaris: Int,
    val jumlahBarisKosong: Int,
    val jumlahBarisTidakKosong: Int,
    val jumlahKata: Int,
    val jumlahKarakter: Int,
    val panjangBarisTerpanjang: Int,
    val jumlahBarisKode: Int
)
