package cl.example.piamonteinventario

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // La URL base ahora puede cambiar
    private var baseUrl = "http://localhost:3000/" // URL temporal
    private var retrofit: Retrofit? = null

    // Función para actualizar la IP cuando la encontremos
    fun updateBaseUrl(newIp: String, port: Int) {
        baseUrl = "http://$newIp:$port/"
        // Reseteamos la instancia para que se cree de nuevo con la nueva URL
        retrofit = null
    }

    val instance: ApiService
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit!!.create(ApiService::class.java)
        }
}