package com.example.listado.screens.login

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.listado.R
import com.example.listado.navigation.ListadoScreens

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var email by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.forgot_password_title),
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            keyboardController?.hide()
                            navController.popBackStack()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(text = stringResource(id = R.string.email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodySmall,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black
                )
            )

            // Botón para enviar la solicitud
            Button(
                onClick = {
                    keyboardController?.hide()
                    try {
                            viewModel.generarCodigoAlmacenarYEnviarCorreo(email, context)
                            // Mostrar éxito o navegar a la siguiente pantalla
                            Log.d("Listado", "Código enviado con éxito")
                            // Puedes navegar a la siguiente pantalla si es necesario
                            navController.navigate(ListadoScreens.VerifyCode.name)
                    } catch (e: Exception) {
                        // Mostrar mensaje de error
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        Log.d("Listado", "Error: $e")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                enabled = email.isNotBlank()
            ) {
                Text(text = stringResource(id = R.string.send_request))
            }
        }
    }
}