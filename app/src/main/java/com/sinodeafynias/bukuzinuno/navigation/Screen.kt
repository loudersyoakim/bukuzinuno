package com.sinodeafynias.bukuzinuno.navigation

sealed class Screen(val route: String) {
    object Daftar : Screen("daftar")
    object Kategori : Screen("kategori")
    object Cari : Screen("cari")
    object Favorit : Screen("favorit")
    object Pengaturan : Screen("pengaturan")

    object DetailLagu : Screen("detail_lagu/{songId}") {
        fun createRoute(songId: String) = "detail_lagu/$songId"
    }
}