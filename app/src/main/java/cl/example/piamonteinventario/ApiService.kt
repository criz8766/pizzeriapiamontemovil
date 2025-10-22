package cl.example.piamonteinventario

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Obtiene la lista completa de ingredientes
    @GET("/api/ingredientes")
    suspend fun getIngredientes(): List<Ingrediente>

    // Actualiza uno o más ingredientes
    @POST("/api/ingredientes/update")
    suspend fun updateIngredientes(@Body updates: List<Ingrediente>): Response<Unit>

    // --- ¡NUEVO! ---
    // Endpoint para comprobar si el servidor está activo
    @GET("/ping")
    suspend fun pingServer(): Response<Unit>
}