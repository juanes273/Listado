package com.example.listado.screens.login

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listado.model.User
import com.example.listado.navigation.ListadoScreens
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import com.google.firebase.Timestamp
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale


class LoginViewModel : ViewModel() {
    private val auth: FirebaseAuth = Firebase.auth
    private val loading = MutableLiveData(false)
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var userProfileImageResId: Int by mutableStateOf(0)

    fun singInEmailPassword(email: String, password: String, home: () -> Unit) = viewModelScope.launch {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Listado", "signInWithEmailAndPassword logueado")
                        home()
                    } else {
                        Log.d("Listado", "signInWithEmailAndPassword ${task.result.toString()} ")
                    }
                }
        } catch (ex: Exception) {
            Log.d("Listado", "signInWithEmailAndPassword ${ex.message}")
        }
    }

    fun createUSerWithEmailPassword(
        email: String,
        password: String,
        home: () -> Unit
    ) {
        if (loading.value == false) {
            loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displayName = task.result.user?.email?.split("@")?.get(0)
                        createUser(displayName)
                        home()
                    } else {
                        Log.d("Listado", "createUserWithEmailPassword ${task.result.toString()}")
                    }
                    loading.value = false
                }
        }
    }

    private fun createUser(displayName: String?) {
        val userId = auth.currentUser?.uid

        val user = User(
            userId = userId.toString(),
            displayName = displayName.toString(),
            listName = "Lista uno",
            prioridad = "media",
            id = null
        ).toMap()

        FirebaseFirestore.getInstance().collection("users")
            .add(user)
            .addOnSuccessListener {
                Log.d("listado", "Creado ${it.id}")
            }.addOnFailureListener {
                Log.d("listado", "Ocurrió un error ${it}")
            }
    }

    private fun resetPassword(correoUsuario: String) {
        // Lógica para enviar el enlace de cambio de contraseña utilizando el correoUsuario
        try {
            val auth = FirebaseAuth.getInstance()
            auth.sendPasswordResetEmail(correoUsuario)
            Log.d("CambioContraseña", "Enlace de cambio de contraseña enviado con éxito a $correoUsuario")
            // Puedes manejar el éxito según tus necesidades
        } catch (e: Exception) {
            Log.e("CambioContraseña", "Error al enviar el enlace de cambio de contraseña", e)
            // Puedes manejar el error según tus necesidades
        }
    }


    suspend fun verificarYEnviarLinkCambioContraseña(codigo: String, email: String, context: Context) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Usa coroutineScope para llamar a funciones suspend dentro de una coroutine
            coroutineScope {
                val querySnapshot = db.collection("codigos")
                    .whereEqualTo("email", email)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val correoUsuario = documentSnapshot.getString("email")

                    if (!correoUsuario.isNullOrBlank() && codigo == documentSnapshot.getString("codigo")) {
                        // Verifica si el usuario está vetado
                        esUsuarioVetado(email) { usuarioVetado ->
                            if (!usuarioVetado) {
                                // Verifica si el tiempo es válido
                                tiempoValido(email) { tiempoEsValido ->
                                    if (tiempoEsValido) {

                                        resetPassword(correoUsuario)

                                    } else {
                                        // Si el tiempo no es válido, indica que debe generar un nuevo código
                                        Log.d("Listado", "El tiempo para el código ha expirado. Genera un nuevo código.")
                                    }
                                }
                            } else {
                                // Usuario vetado, realiza la lógica correspondiente
                                Log.d("Listado", "El usuario está vetado. No se puede realizar el cambio de contraseña.")
                            }
                        }
                    } else {
                        val intentosRestantes = obtenerIntentosRestantes(email)
                        // Incrementa el contador de intentos
                        decrementarContadorIntentos(email)

                        if (intentosRestantes == 1) {
                            // Usuario no vetado
                            bloquearUsuario(email)
                            Log.d("Listado", "Usuario bloqueado. Fecha y hora de bloqueo: ${obtenerFechaHoraActual()}")
                        } else if(intentosRestantes == 0) {
                            Log.d("Listado", "El usuario está bloqueado, no puede realizar esta accion")

                        }else{
                            var intentosMostrar = intentosRestantes-1
                            Log.d("Listado", "Código incorrecto. Intentos restantes: $intentosMostrar")

                        }

                        // Obtiene el número actual de intentos restantes para el código

                        // Verifica si el usuario debe ser bloqueado
                    }
                } else {
                    Log.d("Listado", "No se encontró ningún documento con el correo proporcionado")
                }
            }
        } catch (e: Exception) {
            Log.e("Listado", "Error al verificar el código en la base de datos: ${e.message}")
        }
    }







    suspend fun decrementarContadorIntentos(email: String) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Busca el documento que tiene el campo 'codigo' igual al código proporcionado
            val querySnapshot = db.collection("codigos")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Accede al primer documento encontrado (puedes ajustar según tus necesidades)
                val codigoDocRef = querySnapshot.documents[0].reference

                // Realiza una transacción para garantizar la consistencia
                db.runTransaction { transaction ->
                    val codigoDoc = transaction.get(codigoDocRef)

                    // Verifica si el documento tiene el campo 'intentos'
                    if (codigoDoc.contains("intentos")) {
                        val intentosActuales = codigoDoc.getLong("intentos") ?: 0

                        // Verifica que no sea menor que cero para evitar valores negativos
                        val nuevosIntentos = if (intentosActuales > 0) intentosActuales - 1 else 0

                        // Actualiza el campo 'intentos' en el documento
                        transaction.update(codigoDocRef, "intentos", nuevosIntentos)
                    }
                    // Si el documento no tiene el campo 'intentos', no hace nada

                    // Devuelve un valor arbitrario, ya que no se usa en este caso
                    null
                }.await()
            }
            // Si no se encontró ningún documento con el código, no hace nada
        } catch (e: Exception) {
            Log.e("CambioContraseña", "Error al decrementar el contador de intentos en la base de datos", e)
            // Puedes manejar el error según tus necesidades
        }
    }

    suspend fun obtenerIntentosRestantes(email: String): Int {
        try {
            val db = FirebaseFirestore.getInstance()

            // Busca el documento que tiene el campo 'codigo' igual al código proporcionado
            val querySnapshot = db.collection("codigos")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Accede al primer documento encontrado (puedes ajustar según tus necesidades)
                val codigoDoc = querySnapshot.documents[0]

                // Verifica si el documento tiene el campo 'intentos'
                if (codigoDoc.contains("intentos")) {
                    return codigoDoc.getLong("intentos")?.toInt() ?: 0
                }
                // Si el documento no tiene el campo 'intentos', devuelve un valor predeterminado (puedes ajustar según tus necesidades)
            }
            // Si no se encontró ningún documento con el código, devuelve un valor predeterminado (puedes ajustar según tus necesidades)
        } catch (e: Exception) {
            Log.e("CambioContraseña", "Error al obtener el contador de intentos en la base de datos", e)
            // Puedes manejar el error según tus necesidades
        }
        return 0  // Valor predeterminado en caso de error o si no se encontró el documento
    }

    suspend fun bloquearUsuario(email: String) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Busca el documento que tiene el campo 'codigo' igual al código proporcionado
            val querySnapshot = db.collection("codigos")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!querySnapshot.isEmpty) {
                // Accede al primer documento encontrado (puedes ajustar según tus necesidades)
                val codigoDocRef = querySnapshot.documents[0].reference

                // Actualiza el campo 'veto' en el documento
                codigoDocRef.update("veto", obtenerFechaHoraActual())
                    .addOnSuccessListener {
                        Log.d("BloquearUsuario", "Usuario bloqueado. Fecha de bloqueo: ${obtenerFechaHoraActual()}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("BloquearUsuario", "Error al bloquear al usuario", e)
                        // Puedes manejar el error según tus necesidades
                    }
            }
            // Si no se encontró ningún documento con el código, no hace nada
        } catch (e: Exception) {
            Log.e("BloquearUsuario", "Error general al bloquear al usuario", e)
            // Puedes manejar el error según tus necesidades
        }
    }



    fun enviarCorreo(codigo: String, email: String) {
        val apiKey = "6129963BB1712747E856F8E7E49B4108062966418C78400F94A1D2EEA4070FEAA6F9466B4D8DDC765FAEB5245954BE39"
        val apiUrl = "https://api.elasticemail.com/v2/email/send"

        val payload = listOf(
            "apikey" to apiKey,
            "subject" to "Código de Verificación",
            "from" to "brandjuan72@gmail.com",
            "fromName" to "Juanes",
            "to" to email,
            "bodyHtml" to "<p>Tu código de verificación es: $codigo</p>",
            // Agrega otros parámetros según sea necesario
        )

        // Verificar si existe el usuario y si el tiempo es válido
        existeUsuario(email) { existe ->
            if (!existe) {
                // Si no existe el usuario, siempre enviar el correo
                enviarCorreoRequest(apiUrl, payload)
            } else {
                // Si existe el usuario, verificar el tiempo válido antes de enviar
                tiempoValido(email) { tiempoEsValido ->
                    if (!tiempoEsValido) {
                        // Si el tiempo no es válido, enviar el correo
                        enviarCorreoRequest(apiUrl, payload)
                    } else {
                        // Si el tiempo es válido, no enviar el correo y puedes imprimir un log si es necesario
                        println("El tiempo es válido, no se envía el correo.")
                    }
                }
            }
        }
    }

    private fun enviarCorreoRequest(apiUrl: String, payload: List<Pair<String, String>>) {
        apiUrl.httpPost(payload)
            .response { _, response, result ->
                when (result) {
                    is Result.Success -> {
                        println("Éxito: ${response.statusCode} - ${response.responseMessage}")
                        println(result.value)
                    }
                    is Result.Failure -> {
                        println("Error: ${response.statusCode} - ${response.responseMessage}")
                        println(result.error)
                    }
                }
            }
    }


    fun almacenarEnFirestore(codigo: String, fechaHora: String, userId: String, email: String) {
        // Obtén la referencia de la colección
        val collection = db.collection("codigos")
        val intentos = 4
        val veto: Any? = null // Utiliza Any? para permitir valores nulos

        val data = hashMapOf(
            "codigo" to codigo,
            "fechaHora" to fechaHora,
            "intentos" to intentos,
            "email" to email,
            "veto" to veto
        )

        // Verificar si ya existe un documento con el mismo correo electrónico
        existeUsuario(email) { existe ->
            if (existe) {
                // Si existe, verificar también el tiempo antes de actualizar el documento existente
                tiempoValido(email) { tiempoEsValido ->
                    if (tiempoEsValido) {
                        // Si el tiempo es válido, imprimir un log indicando que hay un código en proceso
                        println("Hay un código en proceso para el usuario con email: $email")
                    } else {
                        // Si el tiempo no es válido, proceder a actualizar el documento existente
                        collection.whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener { documents ->
                                if (!documents.isEmpty) {
                                    val documentId = documents.documents[0].id
                                    collection.document(documentId).set(data)
                                        .addOnSuccessListener {
                                            println("Documento actualizado en Firestore con ID: $documentId")
                                        }
                                        .addOnFailureListener { e ->
                                            println("Error al actualizar el documento en Firestore: $e")
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                println("Error al buscar el documento en Firestore: $e")
                            }
                    }
                }
            } else {
                // Si no existe, agrega un nuevo documento
                collection.add(data)
                    .addOnSuccessListener { documentReference ->
                        println("Código almacenado en Firestore con ID del documento: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        println("Error al almacenar en Firestore: $e")
                    }
            }
        }
    }




    fun generarCodigo(): String {
        val codigo = (100000..999999).random()
        return codigo.toString()
    }

    fun obtenerFechaHoraActual(): String {
        val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formato.format(Date())
    }

    fun generarCodigoAlmacenarYEnviarCorreo(email: String, context: Context) {
        try {
            // Verificar si el usuario está vetado
            esUsuarioVetado(email) { esVetado ->
                if (esVetado) {
                    // Si el usuario está vetado, hacer un log y salir de la función
                    Log.d("Listado", "El usuario está vetado y no puede realizar la acción.")
                    return@esUsuarioVetado
                }

                // Generar código aleatorio de 6 dígitos
                val codigo = generarCodigo()

                // Almacenar en Firebase Cloud Firestore con la fecha, hora y ID del usuario
                val fechaHora = obtenerFechaHoraActual()
                val userId = auth.currentUser?.uid ?: ""
                almacenarEnFirestore(codigo, fechaHora, userId, email)

                // Enviar código por correo electrónico
                enviarCorreo(codigo, email)

                Log.d("Listado", "Código generado y enviado con éxito")
            }
        } catch (e: Exception) {
            Log.e("Listado", "Error al generar y enviar el código: ${e.message}")
        }
    }



    private fun esUsuarioVetado(email: String, callback: (Boolean) -> Unit) {
        val minutosVeto = 30 // Puedes ajustar este valor según tus necesidades

        try {
            val db = FirebaseFirestore.getInstance()
            val codigosCollection = db.collection("codigos")

            // Buscar el documento en la colección "codigos" con el campo "email" igual al proporcionado
            codigosCollection.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Documento encontrado, obtener el valor del campo "veto"
                        val veto = documents.documents[0].get("veto") as String?

                        if (!veto.isNullOrBlank()) {
                            // Verificar si el usuario está vetado y si ha pasado media hora desde la hora de veto
                            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val fechaVetoDate: Date? = formato.parse(veto)

                            if (fechaVetoDate != null) {
                                val fechaVetoLimite = Calendar.getInstance()
                                fechaVetoLimite.time = fechaVetoDate
                                fechaVetoLimite.add(Calendar.MINUTE, minutosVeto)

                                val fechaActual = Calendar.getInstance()

                                callback(fechaActual.before(fechaVetoLimite))
                                return@addOnSuccessListener
                            }
                        }
                    }

                    // Usuario no vetado o campo "veto" nulo
                    callback(false)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al buscar en la base de datos
                    Log.e("Listado", "Error al buscar en la base de datos: ${exception.message}")
                    callback(false)
                }
        } catch (e: Exception) {
            // Manejar otros errores
            Log.e("Listado", "Error en la función esUsuarioVetado: ${e.message}")
            callback(false)
        }
    }

    private fun existeUsuario(email: String, callback: (Boolean) -> Unit) {
        try {
            val db = FirebaseFirestore.getInstance()
            val codigosCollection = db.collection("codigos")

            // Buscar el documento en la colección "codigos" con el campo "email" igual al proporcionado
            codigosCollection.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    callback(!documents.isEmpty)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al buscar en la base de datos
                    Log.e("Listado", "Error al buscar en la base de datos: ${exception.message}")
                    callback(false)
                }
        } catch (e: Exception) {
            // Manejar otros errores
            Log.e("Listado", "Error en la función existeUsuario: ${e.message}")
            callback(false)
        }
    }

    fun tiempoValido(email: String, callback: (Boolean) -> Unit) {
        val minutosLimite = 15 // Puedes ajustar este valor según tus necesidades

        try {
            val codigosCollection = db.collection("codigos")

            // Buscar el código para el email proporcionado
            codigosCollection.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        // Documento encontrado, obtener la fechaHora del código
                        val fechaHora = documents.documents[0].getString("fechaHora")

                        if (!fechaHora.isNullOrBlank()) {
                            // Verificar si han pasado 15 minutos desde la fechaHora
                            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val fechaCodigo: Date? = formato.parse(fechaHora)

                            if (fechaCodigo != null) {
                                val fechaLimite = Calendar.getInstance()
                                fechaLimite.time = fechaCodigo
                                fechaLimite.add(Calendar.MINUTE, minutosLimite)

                                val fechaActual = Calendar.getInstance()

                                callback(!fechaActual.after(fechaLimite))
                                return@addOnSuccessListener
                            }
                        }
                    }

                    // Documento no encontrado, fechaHora nula o campo "fechaHora" vacío
                    callback(false)
                }
                .addOnFailureListener { exception ->
                    // Manejar errores al buscar en la base de datos
                    Log.e("Listado", "Error al buscar en la base de datos: ${exception.message}")
                    callback(false)
                }
        } catch (e: Exception) {
            // Manejar otros errores
            Log.e("Listado", "Error en la función tiempoValido: ${e.message}")
            callback(false)
        }
    }



}
