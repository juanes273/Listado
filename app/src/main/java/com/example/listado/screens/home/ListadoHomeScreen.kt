package com.example.listado.screens.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listado.navigation.ListadoScreens

@Composable
fun ListadoHomeScreen(navController: NavController) {
    val viewModel: ListadoViewModel = viewModel()
    val newObjectTextState = remember { mutableStateOf(TextFieldValue()) }
    val emailTextState = remember { mutableStateOf(TextFieldValue()) }
    val context = LocalContext.current

    Column {
        Text(text = "Listado home screen")
        Text(text = "List Name: ${viewModel.listName}")

        // Text field for entering the email
        BasicTextField(
            value = emailTextState.value,
            onValueChange = {
                emailTextState.value = it
            },
            modifier = Modifier.padding(16.dp)
        )

        // Button to add the new object
        Button(
            onClick = {
                val newObject = newObjectTextState.value.text
                if (newObject.isNotEmpty()) {
                    viewModel.addObjectToList(newObject)
                    newObjectTextState.value = TextFieldValue() // Clear the input field
                    Log.d("ListadoViewModel", "Objeto agregándose: $newObject")
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Agregar Objeto")
        }

        // Button to send the code by email
        Button(
            onClick = {
                val newObject = newObjectTextState.value.text
                val email = emailTextState.value.text

                if (email.isNotEmpty()) {
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Enviar Código por Correo")
        }

        Button(
            onClick = {
                // Navegar a la pantalla de datos del usuario
                navController.navigate(ListadoScreens.UserDataContent.name)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Ver Datos del Usuario")
        }

        // Button to log out
        Button(
            onClick = {
                // Implement the sign-out logic here
                viewModel.signOut()
                // Optionally, navigate to the login screen after signing out
                navController.navigate(ListadoScreens.LoginScreen.name)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Cerrar Sesión")
        }
    }
}

// Replace this with your actual code generation logic
fun generateTemporaryCode(): String {
    // Implement your code generation logic here (e.g., random alphanumeric code)
    return "123456"
}
