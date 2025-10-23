package cl.example.piamonteinventario

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

object RetrofitClient {

    // La URL base es dinámica y se actualizará cuando encontremos el servidor
    private var baseUrl = "http://localhost:3000/" // URL temporal de respaldo
    private var retrofit: Retrofit? = null

    // Configuramos un cliente HTTP con un tiempo de espera corto para el "ping"
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS) // Tiempo máximo para conectar
        .readTimeout(5, TimeUnit.SECONDS)    // Tiempo máximo para leer una respuesta
        .build()

    // Función para que MainActivity actualice la IP cuando la encuentre
    fun updateBaseUrl(newIp: String, port: Int) {
        baseUrl = "http://$newIp:$port/"
        // Forzamos a que Retrofit se reconstruya en la siguiente llamada
        retrofit = null
    }

    // Propiedad que crea la instancia de Retrofit solo cuando se necesita
    val instance: ApiService
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient) // Usamos nuestro cliente con tiempo de espera
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiService::class.java)
        }
}