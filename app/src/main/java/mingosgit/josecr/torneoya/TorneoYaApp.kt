package mingosgit.josecr.torneoya

import android.app.Application
import com.google.firebase.FirebaseApp

class TorneoYaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
