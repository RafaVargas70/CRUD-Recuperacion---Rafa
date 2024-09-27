package RecyclerViewHelpers

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import rafa.vargas.crud_cajero_rafa_recu.R

class ViewHolder(view: View):RecyclerView.ViewHolder(view) {

    val txtNombre = view.findViewById<TextView>(R.id.txtNombre)
    val imgActualizar = view.findViewById<ImageView>(R.id.imgActualizar)
    val imgEliminar = view.findViewById<ImageView>(R.id.imgEliminar)
}