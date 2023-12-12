package com.example.listado.screens.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


    Column {
        Text(text = "Listado home screen")
        Text(text = "List Name: ${viewModel.listName}")

        BasicTextField(
            value = newObjectTextState.value,
            onValueChange = {
                newObjectTextState.value = it
            },
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, Color.Blue) // Añadir un borde azul
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
