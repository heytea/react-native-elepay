package com.heytea.elepay

/**
 * Package     ：com.heytea.elepay
 * Description ：
 * Company     ：Heytea
 * Author      ：Created by ChengGuang
 * CreateTime  ：2020/9/1.
 */
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.Callback
import jp.elestyle.androidapp.elepay.*
import jp.elestyle.androidapp.elepay.utils.locale.LanguageKey
import org.json.JSONObject
import java.util.*

class RnElepayModule(reactContext: ReactApplicationContext): ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "Elepay"

    @ReactMethod
    fun initElepay(configs: ReadableMap) {
        val pKey = if (configs.hasKey("publicKey")) {
            try { configs.getString("publicKey") ?: "" } catch (e: Exception) { "" }
        } else ""

        val apiUrl = if (configs.hasKey("apiUrl")) {
            try { configs.getString("apiUrl") ?: "" } catch (e: Exception) { "" }
        } else ""

        val googlePayEnv = if (configs.hasKey("googlePayEnvironment")) {
            try {
                configs.getString("googlePayEnvironment")?.let {
                    if (it.toLowerCase().contains("test")) GooglePayEnvironment.TEST
                    else GooglePayEnvironment.PRODUCTION
                }
            } catch (e: Exception) {
                null
            }
        } else null

        val languageKeyStr = if (configs.hasKey("languageKey")) {
            try { configs.getString("languageKey") ?: "" } catch (e: Exception) { "" }
        } else ""
        val languageKey = retrieveLanguageKey(languageKeyStr)

        val themeName = if (configs.hasKey("theme")) {
            try { configs.getString("theme") ?: "" } catch (e: Exception) { "" }
        } else ""
        val theme = retrieveTheme(themeName)

        Elepay.setup(ElepayConfiguration(pKey, apiUrl, googlePayEnv, languageKey, theme))
    }

    @ReactMethod
    fun changeLanguage(langConfig: ReadableMap) {
        val languageKeyStr = if (langConfig.hasKey("languageKey")) {
            try { langConfig.getString("languageKey") ?: "" } catch (e: Exception) { "" }
        } else ""
        val languageKey = retrieveLanguageKey(languageKeyStr)
        Elepay.changeLanguageKey(languageKey)
    }

    @ReactMethod
    fun changeTheme(themeMap: ReadableMap) {
        val themeName = if (themeMap.hasKey("theme")) {
            try { themeMap.getString("theme") ?: "" } catch (e: Exception) { "" }
        } else ""
        val theme = retrieveTheme(themeName)
        Elepay.changeTheme(theme)
    }

    @ReactMethod
    fun handleOpenUrlString(urlString: String) {
    }

    @ReactMethod
    fun handlePaymentWithPayload(payload: String, resultHandler: Callback) {
        Elepay.processPayment(chargeDataString = payload, fromActivity = getCurrentActivity()!!) { result ->
            processElepayResult(result, resultHandler)
        }
    }

    @ReactMethod
    fun handleSourceWithPayload(payload: String, resultHandler: Callback) {
        Elepay.processSource(sourceString = payload, fromActivity = getCurrentActivity()!!) { result ->
            processElepayResult(result, resultHandler)
        }
    }

    @ReactMethod
    fun checkoutWithPayload(payload: String, resultHandler: Callback) {
        Elepay.checkout(checkoutJsonString = payload, fromActivity = getCurrentActivity()!!) { result ->
            processElepayResult(result, resultHandler)
        }
    }

    private fun processElepayResult(result: ElepayResult, resultHandler: Callback) {
        when (result) {
            is ElepayResult.Succeeded ->
                resultHandler.invoke(RnElepayResult("succeeded", result.paymentId).asMap)

            is ElepayResult.Canceled ->
                resultHandler.invoke(RnElepayResult("cancelled", result.paymentId).asMap)

            is ElepayResult.Failed -> {
                val rnElepayError = when (val error = result.error) {
                    is ElepayError.SDKNotSetup ->
                        RnElepayError(error.errorCode, "SDK not setup", error.message)

                    is ElepayError.UnsupportedPaymentMethod ->
                        RnElepayError("", "Unsupported payment method", error.paymentMethod)

                    is ElepayError.AlreadyMakingPayment ->
                        RnElepayError("", "Already making payment", error.paymentId)

                    is ElepayError.InvalidPayload ->
                        RnElepayError(error.errorCode, "Invalid payload", error.message)

                    is ElepayError.UninitializedPaymentMethod ->
                        RnElepayError(
                            error.errorCode,
                            "Uninitialized payment method",
                            "${error.paymentMethod} ${error.message}"
                        )

                    is ElepayError.SystemError ->
                        RnElepayError(error.errorCode, "System Error", error.message)

                    is ElepayError.PaymentFailure ->
                        RnElepayError(error.errorCode, "Payment failure", error.message)

                    is ElepayError.PermissionRequired ->
                        RnElepayError("", "Permissions required", error.permissions.joinToString(", "))
                }
                resultHandler.invoke(
                    RnElepayResult("failed", result.paymentId).asMap,
                    rnElepayError.asMap
                )
            }
        }
    }

    private fun retrieveLanguageKey(languageKeyStr: String): LanguageKey =
        when (languageKeyStr.toLowerCase()) {
            "english" -> LanguageKey.English
            "simplifiedchinise" -> LanguageKey.SimplifiedChinise
            "traditionalchinese" -> LanguageKey.TraditionalChinese
            "japanese" -> LanguageKey.Japanese
            "system" -> LanguageKey.System
            else -> LanguageKey.System
        }

    private fun retrieveTheme(themeName: String): ElepayTheme =
        when(themeName.toLowerCase(Locale.ROOT)) {
            "light" -> ElepayTheme.Light
            "dark" -> ElepayTheme.Dark
            else -> ElepayTheme.System
        }
}