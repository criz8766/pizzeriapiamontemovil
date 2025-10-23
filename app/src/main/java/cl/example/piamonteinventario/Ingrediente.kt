package cl.example.piamonteinventario

data class Ingrediente(
    val id: Int,
    val nombre: String,
    var cantidad: String, // 'var' porque su valor puede cambiar
    val categoria: String,
    // El campo 'comprar' que guardará un 1 (si) o un 0 (no)
    var comprar: Int
)