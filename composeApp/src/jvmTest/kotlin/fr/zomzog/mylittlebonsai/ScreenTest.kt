package fr.zomzog.mylittlebonsai

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ScreenTest {

    @Test
    fun screenHomeToString() {
        assertThat(Screen.Home.toString()).isEqualTo("Home")
    }

    @Test
    fun screenFolderSetupToString() {
        assertThat(Screen.FolderSetup.toString()).isEqualTo("FolderSetup")
    }

    @Test
    fun screenBonsaiListToString() {
        assertThat(Screen.BonsaiList.toString()).isEqualTo("BonsaiList")
    }

    @Test
    fun screenAddBonsaiToString() {
        assertThat(Screen.AddBonsai.toString()).isEqualTo("AddBonsai")
    }
}
