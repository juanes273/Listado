package com.example.listado.screens.home

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.listado.navigation.ListadoScreens
import kotlinx.coroutines.delay
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoHomeScreen(navController: NavController) {
    val viewModel: ListadoViewModel = viewModel()
    val newObjectTextState = remember { mutableStateOf(TextFieldValue()) }

    // Estado local para almacenar la lista de objetos
    var objectList by remember { mutableStateOf(emptyList<String>()) }

    // Obtén el valor actualizado del ViewModel
    val currentObjectList by rememberUpdatedState(newValue = viewModel.objectList.value)

    var lists by remember{ mutableStateOf(emptyList<String>())}

    val currentLists by rememberUpdatedState(newValue = viewModel.listNames.value)

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    var selectedText by remember { mutableStateOf("Selecciona lista") }


    // Actualiza el estado local cuando la lista de objetos cambia
    DisposableEffect(currentObjectList) {
        objectList = currentObjectList ?: emptyList()
        onDispose { }
    }

    // Actualiza el estado local cuando la lista de listas cambia
    DisposableEffect(currentLists) {
        lists = currentLists ?: emptyList()
        onDispose { }
    }

    // Llamada a fetchObjectList para obtener la lista de objetos
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchObjectList()
    }

    // Llamada a fetchObjectList para obtener la lista de listas
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchObjectLists()
    }

    // Observa los cambios en el LiveData _objectList
    LaunchedEffect(viewModel.objectList.value) {
        viewModel.fetchObjectList()
    }

    // Observa los cambios en el LiveData _listNames
    LaunchedEffect(viewModel.listNames.value) {
        viewModel.fetchObjectLists()
    }

    // Estado para controlar el retraso
    var shouldFetchList by remember { mutableStateOf(false) }


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
                    shouldFetchList = true
                    newObjectTextState.value = TextFieldValue() // Clear the input field
                    Log.d("ListadoViewModel", "Objeto agregándose: $newObject")
                }
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Agregar Objeto")
        }

        // Llamada a fetchObjectList con retraso
        LaunchedEffect(shouldFetchList) {
            if (shouldFetchList) {
                delay(1000) // Retraso de 3 segundos
                Log.d("ListadoViewModel", "Valor de la lista antes de fetchObjectList: $objectList")
                viewModel.fetchObjectList()
                Log.d("ListadoViewModel", "Valor de la lista después de fetchObjectList: ${viewModel.objectList.value}")
                shouldFetchList = false
            }
        }

        LazyColumn {
            items(objectList) { // Utiliza objectList para mostrar los elementos
                Text(text = it)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                }
            ) {
                TextField(
                    value = selectedText,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    lists.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(text = item) },
                            onClick = {
                                selectedText = item
                                expanded = false
                                Toast.makeText(context, item, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
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





