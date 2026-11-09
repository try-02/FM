package com.crouton.alpha.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KeamananArsipTest {
    @Test fun tolakZipSlip() {
        assertFalse(KeamananArsip.adalahPathAman("../evil.txt"))
        assertFalse(KeamananArsip.adalahPathAman("a/../../evil.txt"))
        assertFalse(KeamananArsip.adalahPathAman("/abs.txt"))
    }

    @Test fun terimaPathNormal() {
        assertTrue(KeamananArsip.adalahPathAman("folder/file.txt"))
        assertTrue(KeamananArsip.dalamBatasKedalaman("a/b/c.txt"))
    }

    @Test fun matriksZipDapatUbah() {
        assertTrue(MatriksArsip.cari("ZIP")?.dapatUbahIsi == true)
        assertTrue(MatriksArsip.cari("RAR")?.dapatBaca == false)
    }
}
