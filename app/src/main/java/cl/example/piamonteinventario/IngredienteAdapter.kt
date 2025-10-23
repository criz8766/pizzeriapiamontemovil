package cl.example.piamonteinventario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredienteAdapter(
    private var ingredientes: List<Ingrediente>,
    private val onEditClick: (Ingrediente) -> Unit,
    private val onBuyClick: (Ingrediente) -> Unit
) : RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder>() {

    inner class IngredienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val cantidad: TextView = itemView.findViewById(R.id.textViewCantidad)
        val infoLayout: LinearLayout = itemView.findViewById(R.id.infoLayout)
        // --- ¡NUEVO! Referencia al CheckBox ---
        val comprarCheckBox: CheckBox = itemView.findViewById(R.id.checkBoxComprar)

        fun bind(ingrediente: Ingrediente) {
            nombre.text = ingrediente.nombre
            cantidad.text = "Cantidad: ${ingrediente.cantidad}"

            // Configura el estado del CheckBox sin disparar el listener
            comprarCheckBox.setOnCheckedChangeListener(null)
            comprarCheckBox.isChecked = ingrediente.comprar == 1

            // Asigna el evento de clic al texto para editar la cantidad
            infoLayout.setOnClickListener { onEditClick(ingrediente) }

            // Asigna el evento de clic al CheckBox para actualizar el estado de compra
            comprarCheckBox.setOnCheckedChangeListener { _, isChecked ->
                ingrediente.comprar = if (isChecked) 1 else 0
                onBuyClick(ingrediente)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ingrediente, parent, false)
        return IngredienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: IngredienteViewHolder, position: Int) {
        holder.bind(ingredientes[position])
    }

    override fun getItemCount() = ingredientes.size

    fun updateData(newIngredientes: List<Ingrediente>) {
        this.ingredientes = newIngredientes
        notifyDataSetChanged()
    }
}