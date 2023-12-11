package com.example.listado.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.listado.screens.ListadoSplashScreen
import com.example.listado.screens.home.ListadoHomeScreen
import com.example.listado.screens.home.UserDataContent
import com.example.listado.screens.login.ForgotPasswordScreen
import com.example.listado.screens.login.ListadoLoginScreen
import com.example.listado.screens.login.VerifyCodeScreen

@Composable
fun ListadoNavigation(){
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ListadoScreens.SplashScreen.name
    ){
        composable(ListadoScreens.SplashScreen.name){
            ListadoSplashScreen(navController = navController)
        }
        composable(ListadoScreens.LoginScreen.name){
            ListadoLoginScreen(navController = navController)
        }
        composable(ListadoScreens.HomeScreen.name){
            ListadoHomeScreen(navController = navController)
        }
        composable(ListadoScreens.ForgotPasswordScreen.name){
            ForgotPasswordScreen(navController = navController)
        }
        composable(ListadoScreens.UserDataContent.name){
            UserDataContent(navController = navController)
        }
        composable(ListadoScreens.VerifyCode.name){
            VerifyCodeScreen(navController = navController)
        }
    }
}