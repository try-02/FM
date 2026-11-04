package id.manajerberkas.kode.presentation.state

import id.manajerberkas.kode.domain.usecase.AmbilPreferensiEditorUseCase
import id.manajerberkas.kode.domain.usecase.AmbilRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.BacaTeksUseCase
import id.manajerberkas.kode.domain.usecase.DapatkanMetadataBerkasUseCase
import id.manajerberkas.kode.domain.usecase.HitungStatistikTeksUseCase
import id.manajerberkas.kode.domain.usecase.HapusRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.SimpanPreferensiEditorUseCase
import id.manajerberkas.kode.domain.usecase.SimpanRiwayatPencarianUseCase
import id.manajerberkas.kode.domain.usecase.SimpanTeksUseCase

class DependensiEditor(
    val bacaTeks: BacaTeksUseCase,
    val simpanTeks: SimpanTeksUseCase,
    val hitungStatistik: HitungStatistikTeksUseCase,
    val dapatkanMetadata: DapatkanMetadataBerkasUseCase,
    val ambilRiwayat: AmbilRiwayatPencarianUseCase,
    val simpanRiwayat: SimpanRiwayatPencarianUseCase,
    val hapusRiwayat: HapusRiwayatPencarianUseCase,
    val ambilPreferensi: AmbilPreferensiEditorUseCase,
    val simpanPreferensi: SimpanPreferensiEditorUseCase
)