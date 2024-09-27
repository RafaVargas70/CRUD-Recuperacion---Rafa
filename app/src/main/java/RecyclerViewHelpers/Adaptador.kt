package RecyclerViewHelpers

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.tbCajero
import rafa.vargas.crud_cajero_rafa_recu.MainActivity
import rafa.vargas.crud_cajero_rafa_recu.R

class Adaptador(var Datos:List<tbCajero>):RecyclerView.Adapter<ViewHolder>() {

    fun actualizarPantalla(uuid:String, nuevoNombre:String) {
        val index = Datos.indexOfFirst { it.uuid == uuid }
        Datos[index].nombreCajero = nuevoNombre
        notifyDataSetChanged()
    }


    fun eliminarRegistro(nombreCajero:String, posicion:Int) {

        val listaDatos = Datos.toMutableList()
        listaDatos.removeAt(posicion)

        GlobalScope.launch(Dispatchers.IO) {
            val objConexion = ClaseConexion().cadenaConexion()

            val deleteUsuario = objConexion?.prepareStatement("delete tbCajero where nombre_cajero = ?")!!
            deleteUsuario.setString(1, nombreCajero)
            deleteUsuario.executeUpdate()

            val commit = objConexion.prepareStatement("Commit")
            commit.executeUpdate()
        }

        Datos = listaDatos.toList()

        notifyItemRemoved(posicion)

        notifyDataSetChanged()
    }

    fun actualizarCajero(nombreCajero: String, uuid:String) {

        GlobalScope.launch(Dispatchers.IO){

            val objConexion = ClaseConexion().cadenaConexion()

            val updateCajero = objConexion?.prepareStatement("update tbCajero set nombre_cajero = ? where uuid_cajero = ?")!!

            updateCajero.setString(1, nombreCajero)
            updateCajero.setString(2, uuid)
            updateCajero.executeUpdate()

            val commit = objConexion.prepareStatement("Commit")
            commit.executeUpdate()


            withContext(Dispatchers.Main){
                actualizarPantalla(uuid, nombreCajero)
            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_card, parent, false)
        return ViewHolder(vista)

    }

    override fun getItemCount() = Datos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = Datos[position]

        holder.txtNombre.text = item.nombreCajero

        holder.imgEliminar.setOnClickListener {
            val contexto = holder.itemView.context

            val builder = AlertDialog.Builder(contexto)
            builder.setTitle("Eliminar")
            builder.setMessage("Estas seguro que deseas eliminar")


            builder.setPositiveButton("Si") {
                dialog, wich ->
                eliminarRegistro(item.nombreCajero, position)
            }

            builder.setNegativeButton("No") {
                dialog, wich ->

                dialog.dismiss()
            }

            builder.show()
        }

        holder.imgActualizar.setOnClickListener {
            val contexto = holder.itemView.context

            val builder = AlertDialog.Builder(contexto)

            builder.setTitle("Actualizar")
            builder.setMessage("¿Desea actualizar el cajero?")

            val cuadroTexto = EditText(contexto)


            cuadroTexto.setHint(item.nombreCajero)

            builder.setView(cuadroTexto)

            builder.setPositiveButton("Actualizar") {
                dialog, wich ->

                actualizarCajero(cuadroTexto.text.toString(), item.uuid)
            }


            builder.setNegativeButton("Cancelar") {
                    dialog, wich ->

                dialog.dismiss()
            }

            builder.show()
        }



    }


}