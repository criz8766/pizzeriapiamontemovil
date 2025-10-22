package cl.example.piamonteinventario // <-- LÍNEA CORREGIDA

data class Ingrediente(
    val id: Int,
    val nombre: String,
    var cantidad: String,
    val categoria: String
)