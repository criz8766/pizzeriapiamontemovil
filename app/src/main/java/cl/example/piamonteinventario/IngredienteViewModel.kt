package cl.example.piamonteinventario

import android.util.Log // <-- LÍNEA AÑADIDA
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {

    private val _ingredientes = MutableLiveData<List<Ingrediente>>()
    val ingredientes: LiveData<List<Ingrediente>> = _ingredientes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _serverConnectionLost = MutableLiveData<Boolean>()
    val serverConnectionLost: LiveData<Boolean> = _serverConnectionLost

    private var heartbeatJob: Job? = null

    fun fetchIngredientes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.instance.getIngredientes()
                _ingredientes.value = response
                startHeartbeat()
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
                stopHeartbeat()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateIngrediente(ingrediente: Ingrediente) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateIngredientes(listOf(ingrediente))
                fetchIngredientes()
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
                checkConnection()
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                delay(5000)
                checkConnection()
            }
        }
    }

    fun stopHeartbeat() {
        heartbeatJob?.cancel()
    }

    private fun checkConnection() {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.pingServer()
            } catch (e: Exception) {
                Log.e("ViewModel", "Heartbeat fallido, conexión perdida.")
                _serverConnectionLost.value = true
                stopHeartbeat()
            }
        }
    }

    fun clearData() {
        _ingredientes.value = emptyList()
        _serverConnectionLost.value = false
    }

    override fun onCleared() {
        super.onCleared()
        stopHeartbeat()
    }
}