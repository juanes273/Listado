package com.example.listado.screens.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.Date
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.UUID

class ListadoViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var listName by mutableStateOf("Loading...")
    private var userDataMap by mutableStateOf<Map<String, String>>(emptyMap())
    private val _imageUrl = mutableStateOf<String?>(null)


    val imageUrl: State<String?> get() = _imageUrl


    init {
        fetchData()
    }

    // Función para actualizar la URL de la imagen en el MutableState
    private fun updateImageUrl(newImageUrl: String) {
        _imageUrl.value = newImageUrl
    }

    // Cambia el nombre del método en tu ListadoViewModel
    private fun updateListName(newListName: String) {
        viewModelScope.launch(Dispatchers.Main) {
            listName = newListName
        }
    }

    // Método para obtener datos de Firestore
    fun fetchData() {
        val userId = auth.currentUser?.uid
        viewModelScope.launch(Dispatchers.IO) {
            if (userId != null) {
                try {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val document: DocumentSnapshot? = querySnapshot.documents[0]
                        if (document != null) {
                            val newListName = document.getString("list_name") ?: "No data available"

                            // Actualizar el nombre de la lista de manera segura
                            updateListName(newListName)
                        }
                    } else {
                        updateListName("No data available")
                    }
                } catch (e: Exception) {
                    // Manejar errores
                    Log.e("ListadoViewModel", "Error al obtener datos del usuario", e)
                }
            }
        }
    }

    fun addObjectToList(newObject: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val document: DocumentSnapshot? = querySnapshot.documents[0]
                        if (document != null) {
                            // Obtén la lista actual de objetos y agrega el nuevo objeto
                            val currentObjects = document.get("objects") as? MutableList<String>
                            currentObjects?.add(newObject)

                            // Crea un mapa con el campo "objects" actualizado
                            val updatedData = mapOf("objects" to currentObjects)

                            // Actualiza el documento con el nuevo mapa
                            document.reference.update(updatedData)

                            Log.d("ListadoViewModel", "Objeto agregado exitosamente a la base de datos: $newObject")

                            // Imprime el primer valor de la lista después de la actualización
                            val firstValue = currentObjects?.firstOrNull()
                            Log.d("ListadoViewModel", "Primer valor de la lista después de la actualización: $firstValue")
                        }
                    } else {
                        Log.e("ListadoViewModel", "No se encontraron documentos para el usuario con ID: $userId")
                    }
                } catch (e: Exception) {
                    Log.e("ListadoViewModel", "Error al agregar objeto a la base de datos", e)
                }
            }
        } else {
            Log.e("ListadoViewModel", "El usuario actual es nulo. Asegúrate de estar autenticado.")
        }
    }

    fun signOut() {
        auth.signOut()
        // Lógica adicional si es necesario
        Log.d("ListadoViewModel", "Usuario cerró sesión")
    }

    fun fetchUserData() {
        // Asegúrate de obtener el ID del usuario actual
        val userId = auth.currentUser?.uid

        // Utiliza viewModelScope para manejar las operaciones asíncronas
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId != null) {
                    // Realiza la consulta a Firestore de manera asíncrona
                    val querySnapshot = db.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]

                        // Mapea los datos del documento a un mapa
                        val userData = documentSnapshot.data as? Map<String, String>

                        // Utiliza withContext para cambiar al hilo principal antes de actualizar el estado
                        withContext(Dispatchers.Main) {
                            // Actualiza la propiedad del ViewModel de manera segura
                            userDataMap = userData ?: emptyMap()
                        }
                    } else {
                        // No se encontraron datos para el usuario
                        Log.e("ListadoViewModel", "No hay datos del usuario $userId")
                        // Puedes manejar esto según tus necesidades
                    }
                }
            } catch (e: Exception) {
                // Manejar errores
                Log.e("ListadoViewModel", "Error al obtener datos del usuario", e)
            }
        }
    }


    fun updateUserData(updates: Map<String, Any>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userDocument = db.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                    if (!userDocument.isEmpty) {
                        val documentSnapshot = userDocument.documents[0]

                        // Actualizar el documento con el mapa de actualizaciones
                        documentSnapshot.reference.update(updates)

                        Log.d("ListadoViewModel", "Datos del usuario actualizados exitosamente")
                    } else {
                        Log.e("ListadoViewModel", "No se encontraron documentos para el usuario con ID: $userId")
                    }
                } catch (e: Exception) {
                    // Manejar errores
                    Log.e("ListadoViewModel", "Error al actualizar datos del usuario", e)
                }
            }
        }
    }

    // Dentro de tu ListadoViewModel
    fun deleteAccount() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Obtén la referencia al documento que tiene el campo "user_id" igual a userId
                    val userDocument = db.collection("users")
                        .whereEqualTo("user_id", userId)
                        .get()
                        .await()

                    // Asegúrate de que se encontró al menos un documento
                    if (!userDocument.isEmpty) {
                        // Elimina el primer documento encontrado
                        val documentToDelete = userDocument.documents[0]
                        documentToDelete.reference.delete().await()

                        // Elimina la cuenta del usuario en Firebase Authentication
                        auth.currentUser?.delete()?.await()

                        // Puedes agregar más lógica según tus necesidades
                        Log.d("ListadoViewModel", "Cuenta eliminada exitosamente: $userId")
                    } else {
                        Log.e("ListadoViewModel", "No se encontró el documento para el usuario: $userId")
                        // Puedes manejar esto según tus necesidades
                    }
                } catch (e: Exception) {
                    Log.e("ListadoViewModel", "Error al eliminar la cuenta", e)
                    // Maneja el error según tus necesidades
                }
            }
        }
    }

    // Inside your ListadoViewModel
    fun uploadImage(context: Context, imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    // Obtain a reference for the image in Firebase Storage
                    val imageFileName = "$userId"
                    val storageRef: StorageReference =
                        FirebaseStorage.getInstance().getReference("images/$imageFileName")

                    // Upload the image to Firebase Storage using putFile
                    storageRef.putFile(imageUri).await()

                    // Obtain the URL of the stored image
                    val downloadUrl = storageRef.downloadUrl.await().toString()

                    // Update the "imageUrl" field in Firestore with the image URL
                    val updates = mapOf("imageUrl" to downloadUrl)
                    updateUserData(updates)

                    // Update the imageUrl in the viewModel
                    updateImageUrl(downloadUrl)

                    Log.d("ListadoViewModel", "Image uploaded successfully: $downloadUrl")
                } catch (e: StorageException) {
                    // Handle storage-related errors
                    Log.e("ListadoViewModel", "Error uploading the image", e)
                } catch (e: Exception) {
                    // Handle other exceptions
                    Log.e("ListadoViewModel", "Error uploading the image", e)
                }
            }
        }
    }







    // Funciones para acceder a datos específicos del usuario
    fun getUserName(): String {
        return userDataMap["display_name"] ?: "nombre no disponible"
    }

    fun getUserEmail(): String {
        return userDataMap["email"] ?: "prioridad no disponible"
    }

    fun getUserResidence(): String {
        return userDataMap["residencia"] ?: "residencia no disponible"
    }

    fun updateDisplayName(newDisplayName: String) {
        val updates = mapOf("display_name" to newDisplayName)
        updateUserData(updates)
    }

    fun updateEmail(newEmail: String) {
        val updates = mapOf("email" to newEmail)
        updateUserData(updates)
    }

    fun updateResidence(newResidence: String) {
        val updates = mapOf("residence" to newResidence)
        updateUserData(updates)
    }


}
