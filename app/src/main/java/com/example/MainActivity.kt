package com.example

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBg

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Configure Firebase App Check with launch intent before any Compose UI or ViewModels
    configureAppCheck(this, intent)

    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
    )
    setContent {
      MyApplicationTheme {
        val aiCoachViewModel: AiCoachViewModel = viewModel()
        val aiUiState by aiCoachViewModel.uiState.collectAsStateWithLifecycle()
        val playerContext by aiCoachViewModel.playerContext.collectAsStateWithLifecycle()
        var showAiCoach by remember { mutableStateOf(false) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          containerColor = ObsidianBg,
          contentWindowInsets = WindowInsets.safeDrawing,
        ) { innerPadding ->
          GamingAssistantPanelHost(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            isAiCoachOpen = showAiCoach,
            onOpenAiCoach = { contextSummary ->
              if (contextSummary.isNotBlank()) {
                aiCoachViewModel.updatePlayerContext(contextSummary)
              }
              showAiCoach = true
            },
            onDismissAiCoach = { showAiCoach = false },
            onExitApp = { finish() },
          )

          if (showAiCoach) {
            AiCoachSheet(
              uiState = aiUiState,
              playerContext = playerContext,
              onDismiss = { showAiCoach = false },
              onAskPrompt = { prompt -> aiCoachViewModel.askCoach(prompt) },
              onClear = { aiCoachViewModel.clearResponse() },
            )
          }
        }
      }
    }
  }
}

private class AndroidPanelBridge(
  private val context: Context,
  private val onOpenAiCoachCallback: (String) -> Unit,
) {
  private val mainHandler = Handler(Looper.getMainLooper())

  @JavascriptInterface
  fun copyToClipboard(text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("FF India Pro", text))
  }

  @JavascriptInterface
  fun openAiCoach(contextSummary: String) {
    mainHandler.post { onOpenAiCoachCallback(contextSummary) }
  }
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun GamingAssistantPanelHost(
  modifier: Modifier = Modifier,
  isAiCoachOpen: Boolean,
  onOpenAiCoach: (String) -> Unit,
  onDismissAiCoach: () -> Unit,
  onExitApp: () -> Unit,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var isLoaded by remember { mutableStateOf(false) }
  var loadError by remember { mutableStateOf<String?>(null) }
  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  val htmlContent =
    remember(context) {
      runCatching {
          context.assets.open("ff_india_pro_panel.html").bufferedReader().use { it.readText() }
        }
        .getOrNull()
    }

  DisposableEffect(lifecycleOwner, webViewRef) {
    val observer = LifecycleEventObserver { _, event ->
      val wv = webViewRef ?: return@LifecycleEventObserver
      when (event) {
        Lifecycle.Event.ON_RESUME -> wv.onResume()
        Lifecycle.Event.ON_PAUSE -> wv.onPause()
        Lifecycle.Event.ON_DESTROY -> {
          wv.stopLoading()
          wv.destroy()
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  BackHandler {
    if (isAiCoachOpen) {
      onDismissAiCoach()
      return@BackHandler
    }
    val wv = webViewRef
    if (wv == null) {
      onExitApp()
    } else {
      wv.evaluateJavascript("window.handleAndroidBack ? window.handleAndroidBack() : 'exit'") {
        result ->
        if (result?.trim('"') == "exit") {
          onExitApp()
        }
      }
    }
  }

  Box(modifier = modifier.background(ObsidianBg)) {
    if (htmlContent == null || loadError != null) {
      Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = loadError ?: "Unable to load gaming panel asset.",
          color = MaterialTheme.colorScheme.onBackground,
          style = MaterialTheme.typography.bodyLarge,
        )
        Row(modifier = Modifier.padding(top = 16.dp)) {
          Button(
            onClick = {
              loadError = null
              isLoaded = false
              htmlContent?.let { html ->
                webViewRef?.loadDataWithBaseURL(
                  "https://ffindiapro.local/",
                  html,
                  "text/html",
                  "UTF-8",
                  null,
                )
              }
            },
            modifier = Modifier.testTag("retry_load_button"),
          ) {
            Text("Retry")
          }
          Spacer(modifier = Modifier.width(12.dp))
          Button(
            onClick = { onOpenAiCoach("") },
            modifier = Modifier.testTag("open_ai_coach_button"),
          ) {
            Text("AI Coach")
          }
        }
      }
    } else {
      AndroidView(
        factory = { ctx ->
          try {
            WebView(ctx).apply {
              setBackgroundColor(AndroidColor.parseColor("#06080F"))
              overScrollMode = WebView.OVER_SCROLL_NEVER
              isFocusable = true
              isFocusableInTouchMode = true
              isClickable = true
              isLongClickable = true
              setOnTouchListener { view, event ->
                when (event.actionMasked) {
                  MotionEvent.ACTION_DOWN -> {
                    view.parent?.requestDisallowInterceptTouchEvent(true)
                    if (!view.hasFocus()) {
                      view.requestFocus()
                    }
                  }
                  MotionEvent.ACTION_UP,
                  MotionEvent.ACTION_CANCEL -> {
                    view.parent?.requestDisallowInterceptTouchEvent(false)
                  }
                }
                false
              }
              settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                builtInZoomControls = false
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
              }
              addJavascriptInterface(
                AndroidPanelBridge(ctx, onOpenAiCoach),
                "AndroidBridge",
              )
              webChromeClient = WebChromeClient()
              webViewClient =
                object : WebViewClient() {
                  override fun onPageCommitVisible(view: WebView?, url: String?) {
                    isLoaded = true
                  }

                  override fun onPageFinished(view: WebView?, url: String?) {
                    isLoaded = true
                  }
                }
              loadDataWithBaseURL(
                "https://ffindiapro.local/",
                htmlContent,
                "text/html",
                "UTF-8",
                null,
              )
              webViewRef = this
            }
          } catch (t: Throwable) {
            loadError = t.localizedMessage ?: "WebView initialization failed"
            android.view.View(ctx)
          }
        },
        modifier = Modifier.fillMaxSize().testTag("gaming_panel_webview"),
      )

      AnimatedVisibility(
        visible = !isLoaded && loadError == null,
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center),
      ) {
        CircularProgressIndicator(
          color = NeonCyan,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      }
    }
  }
}
