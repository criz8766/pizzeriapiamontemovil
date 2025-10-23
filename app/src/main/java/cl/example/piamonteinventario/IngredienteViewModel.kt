package cl.example.piamonteinventario

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class IngredienteViewModel : ViewModel() {

    private val _ingredientes = MutableLiveData<List<Ingrediente>>()
    val ingredientes: LiveData<List<Ingrediente>> = _ingredientes

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Esta función se llama desde MainActivity una vez que se encuentra el servidor.
    fun fetchIngredientes() {
        // Muestra el ícono de carga solo si la lista está vacía.
        if (_ingredientes.value.isNullOrEmpty()) {
            _isLoading.value = true
        }

        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getIngredientes()
                _ingredientes.value = response
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Se llama cuando se edita la cantidad o se marca el checkbox "Comprar".
    fun updateIngrediente(ingrediente: Ingrediente) {
        viewModelScope.launch {
            try {
                RetrofitClient.instance.updateIngredientes(listOf(ingrediente))
                // Después de una actualización exitosa, recargamos la lista para ver el cambio.
                fetchIngredientes()
            } catch (e: Exception) {
                _error.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    // Limpia los datos de la pantalla cuando se pierde la conexión.
    fun clearData() {
        _ingredientes.value = emptyList()
    }
}