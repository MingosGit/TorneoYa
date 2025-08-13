package mingosgit.josecr.torneoya.data.auth

import mingosgit.josecr.torneoya.data.session.SessionSnapshot

sealed interface AuthState {
    data object SignedOut : AuthState
    data class SignedInCached(val session: SessionSnapshot) : AuthState
    data class SignedInOnline(val session: SessionSnapshot) : AuthState
    data class SignedInNeedsAttention(val session: SessionSnapshot, val reason: String) : AuthState
}
