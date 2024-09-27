package rafa.vargas.crud_cajero_rafa_recu

import RecyclerViewHelpers.Adaptador
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import modelo.ClaseConexion
import modelo.tbCajero
import java.util.UUID
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val txtNombre = findViewById<EditText>(R.id.txtNombre)
        val txtEdad = findViewById<EditText>(R.id.txtEdad)
        val txtPeso = findViewById<EditText>(R.id.txtPeso)
        val txtCorreo = findViewById<EditText>(R.id.txtCorreo)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val rcvCajero = findViewById<RecyclerView>(R.id.rcvCajero)

        rcvCajero.layoutManager = LinearLayoutManager(this)

        fun obtenerCajeros():List<tbCajero> {

            val objConexion = ClaseConexion().cadenaConexion()

            val statement = objConexion?.createStatement()
            val resultSet = statement?.executeQuery("SELECT * FROM tbCajero")!!

            val listaCajeros = mutableListOf<tbCajero>()

            while (resultSet.next()) {
                val uuid = resultSet.getString("UUID_CAJERO")
                val nombreCajero = resultSet.getString("NOMBRE_CAJERO")
                val edadCajero = resultSet.getInt("EDAD_CAJERO")
                val pesoCajero = resultSet.getDouble("PESO_CAJERO")
                val correoCajero = resultSet.getString("CORREO_CAJERO")

                val valoresJuntos = tbCajero(uuid, nombreCajero, edadCajero, pesoCajero, correoCajero)

                listaCajeros.add(valoresJuntos)

            }

            return listaCajeros



        }
        CoroutineScope(Dispatchers.IO).launch {
            val cajeroDB = obtenerCajeros()
            withContext(Dispatchers.Main) {
                val adapter = Adaptador(cajeroDB)
                rcvCajero.adapter = adapter
            }
        }
        // Validación de email usando regex
        val emailPattern = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
        )

        btnGuardar.setOnClickListener {
            val nombre = txtNombre.text.toString().trim()
            val edad = txtEdad.text.toString().trim()
            val peso = txtPeso.text.toString().trim()
            val correo = txtCorreo.text.toString().trim()

            // Validaciones de los campos
            if (nombre.isEmpty()) {
                showToast("El nombre no puede estar vacío")
                return@setOnClickListener
            }

            if (edad.isEmpty() || !isValidInt(edad)) {
                showToast("Edad no válida, debe ser un número")
                return@setOnClickListener
            }

            if (peso.isEmpty() || !isValidFloat(peso)) {
                showToast("Peso no válido, debe ser un número decimal")
                return@setOnClickListener
            }

            if (correo.isEmpty() || !emailPattern.matcher(correo).matches()) {
                showToast("Correo no válido")
                return@setOnClickListener
            }

            // Si todas las validaciones pasan, insertamos los datos en la base de datos
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val objConexion = ClaseConexion().cadenaConexion()

                    val addCajero = objConexion?.prepareStatement(
                        "INSERT INTO tbCajero (UUID_Cajero, Nombre_Cajero, Edad_Cajero, Peso_Cajero, Correo_Cajero) VALUES (?, ?, ?, ?, ?)"
                    )!!

                    addCajero.setString(1, UUID.randomUUID().toString())
                    addCajero.setString(2, nombre)
                    addCajero.setInt(3, edad.toInt())
                    addCajero.setFloat(4, peso.toFloat())
                    addCajero.setString(5, correo)

                    addCajero.executeUpdate()

                    runOnUiThread {
                        showToast("Cajero guardado exitosamente")

                        CoroutineScope(Dispatchers.IO).launch {
                            val cajeroDB = obtenerCajeros()
                            withContext(Dispatchers.Main) {
                                val adapter = Adaptador(cajeroDB)
                                rcvCajero.adapter = adapter
                            }
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        showToast("Error al guardar cajero: ${e.message}")
                    }
                }
            }
        }
    }

    // Función para mostrar mensajes toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Verifica si la cadena es un entero válido
    private fun isValidInt(value: String): Boolean {
        return try {
            value.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    // Verifica si la cadena es un número decimal válido
    private fun isValidFloat(value: String): Boolean {
        return try {
            value.toFloat()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}
