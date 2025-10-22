package cl.example.piamonteinventario // <-- LÍNEA CORREGIDA

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class IngredienteAdapter(
    private var ingredientes: List<Ingrediente>,
    private val onItemClick: (Ingrediente) -> Unit
) : RecyclerView.Adapter<IngredienteAdapter.IngredienteViewHolder>() {

    inner class IngredienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val cantidad: TextView = itemView.findViewById(R.id.textViewCantidad)

        fun bind(ingrediente: Ingrediente) {
            nombre.text = ingrediente.nombre
            cantidad.text = "Cantidad: ${ingrediente.cantidad}"
            itemView.setOnClickListener { onItemClick(ingrediente) }
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