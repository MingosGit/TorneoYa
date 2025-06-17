package mingosgit.josecr.torneoya.data.dao

import androidx.room.*
import mingosgit.josecr.torneoya.data.entities.UsuarioLocalEntity

@Dao
interface UsuarioLocalDao {
    @Query("SELECT * FROM usuario_local WHERE id = 1 LIMIT 1")
    suspend fun getUsuario(): UsuarioLocalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsuario(usuario: UsuarioLocalEntity)
}
