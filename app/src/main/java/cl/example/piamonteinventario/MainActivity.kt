package cl.example.piamonteinventario

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: IngredienteViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var ingredienteAdapter: IngredienteAdapter
    private lateinit var statusText: TextView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var nsdManager: NsdManager
    private val SERVICE_TYPE = "_http._tcp."
    private var isServerFound = false
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var resolveListener: NsdManager.ResolveListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        setupRecyclerView()
        setupSwipeToRefresh()
        viewModel = ViewModelProvider(this)[IngredienteViewModel::class.java]
        observeViewModel()

        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
    }

    private fun setupRecyclerView() {
        ingredienteAdapter = IngredienteAdapter(
            emptyList(),
            onEditClick = { ingrediente ->
                showEditDialog(ingrediente)
            },
            onBuyClick = { ingrediente ->
                viewModel.updateIngrediente(ingrediente)
            }
        )
        recyclerView.adapter = ingredienteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            // Cuando el usuario desliza, forzamos una nueva búsqueda o recarga
            if (isServerFound) {
                viewModel.fetchIngredientes()
            } else {
                stopDiscovery()
                discoverServices()
            }
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun observeViewModel() {
        viewModel.ingredientes.observe(this) { ingredientes ->
            ingredienteAdapter.updateData(ingredientes)
            if (ingredientes.isNotEmpty()) {
                showStatusMessage("", showList = true)
            }
        }
        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showStatusMessage("Cargando...", showList = false)
            }
        }
        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            showStatusMessage("Error de conexión. Desliza para reintentar.")
            isServerFound = false // Marcamos que perdimos el servidor
        }
    }

    private fun initializeDiscoveryListener() {
        if (discoveryListener == null) {
            discoveryListener = object : NsdManager.DiscoveryListener {
                override fun onDiscoveryStarted(regType: String) { Log.d("NSD", "Búsqueda de servicios iniciada.") }
                override fun onServiceFound(service: NsdServiceInfo) {
                    if (!isServerFound && service.serviceType.contains(SERVICE_TYPE) && service.serviceName.contains("Piamonte API Server")) {
                        Log.d("NSD", "Servidor Piamonte encontrado: ${service.serviceName}")
                        initializeResolveListener()
                        nsdManager.resolveService(service, resolveListener)
                    }
                }
                override fun onServiceLost(service: NsdServiceInfo) {
                    Log.e("NSD", "Anuncio de servicio perdido: ${service.serviceName}")
                    if (isServerFound && service.serviceName.contains("Piamonte API Server")) {
                        isServerFound = false
                        viewModel.clearData()
                        runOnUiThread { showStatusMessage("El servidor se ha desconectado. Desliza para buscar de nuevo...") }
                    }
                }
                override fun onDiscoveryStopped(serviceType: String) { Log.i("NSD", "Búsqueda detenida.") }
                override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) { Log.e("NSD", "Error al iniciar búsqueda: $errorCode") }
                override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) { Log.e("NSD", "Error al detener búsqueda: $errorCode") }
            }
        }
    }

    private fun initializeResolveListener() {
        if (resolveListener == null) {
            resolveListener = object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.e("NSD", "Fallo al resolver el servicio: $errorCode")
                    isServerFound = false
                }
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    if (isServerFound) return
                    isServerFound = true
                    val hostAddress = serviceInfo.host.hostAddress
                    val port = serviceInfo.port
                    Log.i("NSD", "Servicio resuelto. IP: $hostAddress, Puerto: $port")
                    RetrofitClient.updateBaseUrl(hostAddress, port)
                    runOnUiThread { viewModel.fetchIngredientes() }
                }
            }
        }
    }

    private fun discoverServices() {
        if (isServerFound) return
        showStatusMessage("Buscando servidor en la red...")
        initializeDiscoveryListener()
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener!!)
    }

    private fun stopDiscovery() {
        if (discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.e("NSD", "Error al detener la búsqueda: ${e.message}")
            }
            discoveryListener = null
            resolveListener = null
        }
    }

    private fun showStatusMessage(message: String, showList: Boolean = false) {
        if (showList) {
            recyclerView.visibility = View.VISIBLE
            statusText.visibility = View.GONE
            progressBar.visibility = View.GONE
        } else {
            statusText.text = message
            statusText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            progressBar.visibility = if (message == "Cargando...") View.VISIBLE else View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isServerFound) {
            discoverServices()
        }
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    private fun showEditDialog(ingrediente: Ingrediente) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Cantidad de ${ingrediente.nombre}")
        val input = EditText(this)
        input.setText(ingrediente.cantidad)
        builder.setView(input)
        builder.setPositiveButton("Guardar") { dialog, _ ->
            val nuevaCantidad = input.text.toString()
            if (nuevaCantidad.isNotBlank()) {
                val ingredienteActualizado = ingrediente.copy(cantidad = nuevaCantidad)
                viewModel.updateIngrediente(ingredienteActualizado)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}