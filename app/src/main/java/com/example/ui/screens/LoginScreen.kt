package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoBlueContainer
import com.example.ui.theme.BentoBluePrimary
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoErrorContainer
import com.example.ui.theme.BentoOutline
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSuccessContainer
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoWarning

@Composable
fun LoginScreen(
  onLogin: (username: String, password: String, rememberMe: Boolean) -> Unit,
  savedUsername: String = "",
  savedPassword: String = "",
  errorMessage: String? = null,
  isLoggingIn: Boolean = false,
  modifier: Modifier = Modifier,
) {
  var username by remember(savedUsername) { mutableStateOf(savedUsername) }
  var password by remember { mutableStateOf("") }
  var rememberMe by remember { mutableStateOf(true) }
  var passwordVisible by remember { mutableStateOf(false) }
  var showDeviceMismatchDialog by remember { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current
  val scrollState = rememberScrollState()

  val currentDeviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(
          brush =
            Brush.verticalGradient(
              colors =
                listOf(
                  Color(0xFFFFFFFF),
                  Color(0xFFF8FAFC),
                  Color(0xFFEEF2F6),
                )
            )
        )
        .testTag("login_screen"),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      // 1. App Icon Badge
      Surface(
        modifier =
          Modifier
            .size(88.dp)
            .shadow(12.dp, CircleShape, spotColor = BentoBluePrimary.copy(alpha = 0.35f)),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(2.dp, BentoBluePrimary.copy(alpha = 0.3f)),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            tint = BentoBluePrimary,
            modifier = Modifier.size(48.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 2. Title & Subtitle
      Text(
        text = "Work Attendance System",
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1E293B),
        textAlign = TextAlign.Center,
      )

      Text(
        text = "Please enter your credentials to sign in",
        fontSize = 13.sp,
        color = BentoTextSecondary,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 3. Login Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
      ) {
        Column(
          modifier = Modifier.padding(22.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          Text(
            text = "Account Sign In",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color =  Color.Black,
          )

          // Username Field
          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", fontWeight = FontWeight.Medium) },
            placeholder = { Text("Enter your username", color = Color(0xFF616161)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = BentoBluePrimary,
              )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("login_username_input"),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = BentoBluePrimary,
                unfocusedLabelColor = Color.Black,
                focusedPlaceholderColor = Color(0xFF616161),
                unfocusedPlaceholderColor = Color(0xFF757575),
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = BentoOutline,
                cursorColor = BentoBluePrimary,
              ),
          )

          // Password Field
          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", fontWeight = FontWeight.Medium) },
            placeholder = { Text("Enter password", color = Color(0xFF616161)) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = BentoBluePrimary,
              )
            },
            trailingIcon = {
              IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                  imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (passwordVisible) "Hide password" else "Show password",
                  tint = Color.Black,
                )
              }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions =
              KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
              ),
            keyboardActions =
              KeyboardActions(
                onDone = {
                  focusManager.clearFocus()
                  if (username.isNotBlank() && password.isNotBlank() && !isLoggingIn) {
                    onLogin(username.trim(), password.trim(), rememberMe)
                  }
                }
              ),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = BentoBluePrimary,
                unfocusedLabelColor = Color.Black,
                focusedPlaceholderColor = Color(0xFF616161),
                unfocusedPlaceholderColor = Color(0xFF757575),
                focusedBorderColor = BentoBluePrimary,
                unfocusedBorderColor = BentoOutline,
                cursorColor = BentoBluePrimary,
              ),
          )

          // Remember Me Checkbox
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(
              checked = rememberMe,
              onCheckedChange = { rememberMe = it },
              colors = CheckboxDefaults.colors(checkedColor = BentoBluePrimary),
              modifier = Modifier.testTag("remember_me_checkbox"),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Remember username",
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = Color.Black,
            )
          }

          // Error Message Banner
          AnimatedVisibility(
            visible = !errorMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
          ) {
            if (errorMessage != null) {
              Box(
                modifier =
                  Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BentoErrorContainer)
                    .padding(12.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = BentoError,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = errorMessage,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoError,
                    lineHeight = 17.sp,
                  )
                }
              }
            }
          }

          // Login Action Button
          Button(
            onClick = {
              focusManager.clearFocus()
              onLogin(username.trim(), password.trim(), rememberMe)
            },
            enabled = username.isNotBlank() && password.isNotBlank() && !isLoggingIn,
            modifier =
              Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = BentoBluePrimary,
                contentColor = Color.White,
              ),
          ) {
            if (isLoggingIn) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp,
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text("Signing in...", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
              Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Device Info Footer
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Default.PhoneAndroid,
          contentDescription = null,
          tint = BentoTextSecondary,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Device: $currentDeviceModel",
          fontSize = 11.sp,
          color = BentoTextSecondary,
        )
      }
    }
  }
}
