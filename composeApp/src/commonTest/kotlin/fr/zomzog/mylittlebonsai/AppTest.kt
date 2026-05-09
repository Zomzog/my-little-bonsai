package fr.zomzog.mylittlebonsai

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class AppTest {

    @Test
    fun appModuleCompiles() {
        assertThat(1 + 1).isEqualTo(2)
    }
}
