package com.example.listado.screens.home

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.listado.R
import com.example.listado.navigation.ListadoScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun UserDataContent(navController: NavController) {
    val viewModel: ListadoViewModel = viewModel()
    viewModel.fetchUserData()

    var showDialogDisplayName by remember { mutableStateOf(false) }
    var nuevoDisplayName by remember { mutableStateOf("") }

    var showDialogResidencia by remember { mutableStateOf(false) }
    var nuevaResidencia by remember { mutableStateOf("") }

    var showDialogEmail by remember { mutableStateOf(false) }
    var nuevoEmail by remember { mutableStateOf("") }

    var showTermsAndConditionsDialog by remember { mutableStateOf(false) }

    val profileImageResId = R.drawable.pp // Reemplaza con tu recurso de imagen
    val painter = painterResource(id = profileImageResId)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child("images/$userId")
    var imageUrl by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    // State to manage the selected image URI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launch the intent to select an image
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        println("Selected Image URI: $uri")
    }

    // Inside YourComposable
    DisposableEffect(selectedImageUri) {
        println("Selected Image URI changed: $selectedImageUri")

        if (selectedImageUri != null) {
            viewModel.uploadImage(context, selectedImageUri!!)
        }

        onDispose {
            // Clean up the state when the composable is disposed
            selectedImageUri = selectedImageUri
        }
    }


    // Función suspendida para obtener la URL de la imagen desde Firebase Storage
    suspend fun getImageUrl(storageReference: StorageReference): String {
        return withContext(Dispatchers.IO) {
            try {
                storageReference.downloadUrl.await().toString()
            } catch (e: Exception) {
                // Manejar errores según sea necesario
                e.printStackTrace()
                ""
            }
        }
    }

    // LaunchedEffect para cargar la imagen cuando cambia la referencia de almacenamiento
    LaunchedEffect(storageReference,viewModel.imageUrl.value) {
        imageUrl = getImageUrl(storageReference)
    }

    // LaunchedEffect para cargar la imagen cada vez que se reevalúa el compositor
    LaunchedEffect(key1 = Unit) {
        imageUrl = viewModel.imageUrl.value
    }


    Surface(modifier = Modifier.fillMaxSize()) {
        // Composable para mostrar la imagen de perfil
        val imageModifier = Modifier
            .size(100.dp)
            .clip(shape = CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Utiliza Coil para cargar la imagen desde Firebase Storage
            imageUrl?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = null,
                    modifier = imageModifier
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Datos del Usuario")

            val userName = viewModel.getUserName()
            val userEmail = viewModel.getUserEmail()
            val userResidencia = viewModel.getUserResidence()

            Text("Nombre: $userName")
            Text("Email: $userEmail")
            Text("Residencia: $userResidencia")

            Spacer(modifier = Modifier.height(16.dp))

            // Button to select an image
            Button(
                onClick = {
                    // Launch the intent to select an image
                    launcher.launch("image/*")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Actualizar foto de perfil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialogDisplayName = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar Nombre")
            }

            if (showDialogDisplayName) {
                AlertDialog(
                    onDismissRequest = { showDialogDisplayName = false },
                    title = { Text("Actualizar Nombre") },
                    text = {
                        TextField(
                            value = nuevoDisplayName,
                            onValueChange = { nuevoDisplayName = it },
                            label = { Text("Nuevo Display Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nuevoDisplayName.isNotBlank()) {
                                    viewModel.updateUserData(mapOf("display_name" to nuevoDisplayName))
                                    showDialogDisplayName = false
                                } else {
                                    // Mostrar un mensaje de error o realizar alguna acción cuando el campo está vacío
                                    // Puedes ajustar esto según tus necesidades
                                }
                            }
                        ) {
                            Text("Actualizar")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialogDisplayName = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialogResidencia = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar Residencia")
            }

            if (showDialogResidencia) {
                AlertDialog(
                    onDismissRequest = { showDialogResidencia = false },
                    title = { Text("Actualizar Residencia") },
                    text = {
                        TextField(
                            value = nuevaResidencia,
                            onValueChange = { nuevaResidencia = it },
                            label = { Text("Nueva Residencia") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nuevaResidencia.isNotBlank()) {
                                    viewModel.updateUserData(mapOf("residencia" to nuevaResidencia))
                                    showDialogResidencia = false
                                } else {
                                    // Mostrar un mensaje de error o realizar alguna acción cuando el campo está vacío
                                    // Puedes ajustar esto según tus necesidades
                                }
                            }
                        ) {
                            Text("Actualizar")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialogResidencia = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showDialogEmail = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Actualizar Email")
            }

            if (showDialogEmail) {
                AlertDialog(
                    onDismissRequest = { showDialogEmail = false },
                    title = { Text("Actualizar Email") },
                    text = {
                        TextField(
                            value = nuevoEmail,
                            onValueChange = { nuevoEmail = it },
                            label = { Text("Nuevo Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nuevoEmail.isNotBlank()) {
                                    viewModel.updateUserData(mapOf("email" to nuevoEmail))
                                    showDialogEmail = false
                                } else {
                                    // Mostrar un mensaje de error o realizar alguna acción cuando el campo está vacío
                                    // Puedes ajustar esto según tus necesidades
                                }
                            }
                        ) {
                            Text("Actualizar")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialogEmail = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate(ListadoScreens.LoginScreen.name) {
                        popUpTo(ListadoScreens.LoginScreen.name) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión")
            }

            Button(
                onClick = {
                    viewModel.deleteAccount()
                    navController.navigate(ListadoScreens.LoginScreen.name) {
                        popUpTo(ListadoScreens.LoginScreen.name) {
                            inclusive = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar Cuenta")
            }

            Button(
                onClick = { showTermsAndConditionsDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 4.dp) // Ajusta los valores de padding según tus preferencias
            ) {
                Text("Términos y condiciones")
            }

            if (showTermsAndConditionsDialog) {
                AlertDialog(
                    onDismissRequest = { showTermsAndConditionsDialog = false },
                    title = { Text("Términos y condiciones") },
                    text = {
                        // Aquí debes agregar el texto de tus términos y condiciones
                        Text("Términos y Condiciones para el Tratamiento de Datos\n" +
                                "\n" +
                                "Aceptación de Términos\n" +
                                "Al utilizar nuestra aplicación, aceptas los términos y condiciones establecidos en este documento. Si no estás de acuerdo con estos términos, por favor, abstente de utilizar nuestra aplicación.\n" +
                                "\n" +
                                "Recopilación de Datos\n" +
                                "Recopilamos información personal limitada y relevante para el funcionamiento de la aplicación. Esto puede incluir, entre otros, tu nombre, dirección de correo electrónico y datos de perfil.\n" +
                                "\n" +
                                "Uso de la Información\n" +
                                "Utilizamos la información recopilada para proporcionar y mejorar nuestros servicios, así como personalizar la experiencia del usuario.\n" +
                                "\n" +
                                "Protección de Datos\n" +
                                "Tomamos medidas para proteger tus datos personales y garantizar su confidencialidad. Sin embargo, no podemos garantizar la seguridad absoluta, por lo que te instamos a tomar precauciones adicionales al compartir información en línea.\n" +
                                "\n" +
                                "Compartir Información\n" +
                                "No compartiremos tu información personal con terceros sin tu consentimiento, excepto cuando sea necesario para cumplir con la ley o proteger nuestros derechos.\n" +
                                "\n" +
                                "Cambios en los Términos\n" +
                                "Nos reservamos el derecho de modificar estos términos en cualquier momento. Te notificaremos sobre cambios significativos a través de nuestra aplicación o por otros medios.\n" +
                                "\n" +
                                "Derechos del Usuario\n" +
                                "Tienes derechos sobre tus datos personales, incluido el acceso, rectificación y eliminación de la información. Puedes ejercer estos derechos según lo permitido por la legislación aplicable.")
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTermsAndConditionsDialog = false }
                        ) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }

}
