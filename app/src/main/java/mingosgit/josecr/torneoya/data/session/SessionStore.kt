package mingosgit.josecr.torneoya.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SESSION_DS = "session_store"

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = SESSION_DS)

data class SessionSnapshot(
    val uid: String = "",
    val email: String = "",
    val nombreUsuario: String = "",
    val avatar: Int? = null,
    val acceptedPrivacy: Boolean = false,
    val acceptedPrivacyAtMillis: Long? = null,
    val privacyVersion: String? = null,
    val privacyUrl: String? = null,
    val isEmailVerified: Boolean? = null,
    val lastTokenRefreshAtMillis: Long? = null
) {
    val hasCachedSession: Boolean get() = uid.isNotBlank()
}

class SessionStore(private val appContext: Context) {

    private object Keys {
        val UID = stringPreferencesKey("uid")
        val EMAIL = stringPreferencesKey("email")
        val NOMBRE = stringPreferencesKey("nombre")
        val AVATAR = intPreferencesKey("avatar") // -1 = null
        val PRIVACY_ACCEPTED = booleanPreferencesKey("privacy_accepted")
        val PRIVACY_ACCEPTED_AT = longPreferencesKey("privacy_accepted_at")
        val PRIVACY_VERSION = stringPreferencesKey("privacy_version")
        val PRIVACY_URL = stringPreferencesKey("privacy_url")
        val EMAIL_VERIFIED = booleanPreferencesKey("email_verified")
        val LAST_TOKEN_REFRESH = longPreferencesKey("last_token_refresh")
    }

    val session: Flow<SessionSnapshot> = appContext.sessionDataStore.data.map { p ->
        SessionSnapshot(
            uid = p[Keys.UID] ?: "",
            email = p[Keys.EMAIL] ?: "",
            nombreUsuario = p[Keys.NOMBRE] ?: "",
            avatar = when (val v = p[Keys.AVATAR]) { null, -1 -> null; else -> v },
            acceptedPrivacy = p[Keys.PRIVACY_ACCEPTED] ?: false,
            acceptedPrivacyAtMillis = p[Keys.PRIVACY_ACCEPTED_AT],
            privacyVersion = p[Keys.PRIVACY_VERSION],
            privacyUrl = p[Keys.PRIVACY_URL],
            isEmailVerified = p[Keys.EMAIL_VERIFIED],
            lastTokenRefreshAtMillis = p[Keys.LAST_TOKEN_REFRESH]
        )
    }

    suspend fun upsert(
        uid: String? = null,
        email: String? = null,
        nombreUsuario: String? = null,
        avatar: Int? = null, // null = guardar null; usa -1 para limpiar
        acceptedPrivacy: Boolean? = null,
        acceptedPrivacyAtMillis: Long? = null,
        privacyVersion: String? = null,
        privacyUrl: String? = null,
        isEmailVerified: Boolean? = null,
        lastTokenRefreshAtMillis: Long? = null
    ) {
        appContext.sessionDataStore.edit { p ->
            uid?.let { p[Keys.UID] = it }
            email?.let { p[Keys.EMAIL] = it }
            nombreUsuario?.let { p[Keys.NOMBRE] = it }
            avatar?.let { p[Keys.AVATAR] = it }
            acceptedPrivacy?.let { p[Keys.PRIVACY_ACCEPTED] = it }
            acceptedPrivacyAtMillis?.let { p[Keys.PRIVACY_ACCEPTED_AT] = it }
            privacyVersion?.let { p[Keys.PRIVACY_VERSION] = it }
            privacyUrl?.let { p[Keys.PRIVACY_URL] = it }
            isEmailVerified?.let { p[Keys.EMAIL_VERIFIED] = it }
            lastTokenRefreshAtMillis?.let { p[Keys.LAST_TOKEN_REFRESH] = it }
        }
    }

    suspend fun clear() {
        appContext.sessionDataStore.edit { it.clear() }
    }
}
