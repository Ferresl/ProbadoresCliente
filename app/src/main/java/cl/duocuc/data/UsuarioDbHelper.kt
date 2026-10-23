package cl.duocuc.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import cl.duocuc.model.RolUsuario
import cl.duocuc.model.Usuario
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class UsuarioDbHelper(context: Context) : SQLiteOpenHelper(context, "usuarios.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuario (correo TEXT PRIMARY KEY, nombre TEXT NOT NULL, rol TEXT NOT NULL, sal TEXT NOT NULL, clave TEXT NOT NULL)"
        )
        val sal = nuevaSal()
        val valores = ContentValues().apply {
            put("correo", "admin@rinconsur.cl")
            put("nombre", "Administrador")
            put("rol", RolUsuario.ADMIN.name)
            put("sal", sal)
            put("clave", resumen("123456", sal))
        }
        db.insertOrThrow("usuario", null, valores)
    }

    override fun onUpgrade(db: SQLiteDatabase, anterior: Int, nueva: Int) {}

    fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        readableDatabase.query(
            "usuario", arrayOf("nombre", "correo", "rol", "sal", "clave"),
            "correo = ?", arrayOf(correo), null, null, null
        ).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val sal = cursor.getString(3)
            if (cursor.getString(4) != resumen(contrasena, sal)) return null
            return Usuario(cursor.getString(0), cursor.getString(1), RolUsuario.valueOf(cursor.getString(2)))
        }
    }

    fun listar(): List<Usuario> {
        val usuarios = mutableListOf<Usuario>()
        readableDatabase.query(
            "usuario", arrayOf("nombre", "correo", "rol"), null, null, null, null, "nombre"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                usuarios.add(Usuario(cursor.getString(0), cursor.getString(1), RolUsuario.valueOf(cursor.getString(2))))
            }
        }
        return usuarios
    }

    fun registrar(usuario: Usuario, contrasena: String): Boolean {
        val sal = nuevaSal()
        val valores = ContentValues().apply {
            put("correo", usuario.correo)
            put("nombre", usuario.nombreCompleto)
            put("rol", usuario.rol.name)
            put("sal", sal)
            put("clave", resumen(contrasena, sal))
        }
        return writableDatabase.insertWithOnConflict(
            "usuario", null, valores, SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    fun eliminar(correo: String): Boolean =
        writableDatabase.delete("usuario", "correo = ?", arrayOf(correo)) > 0

    private fun nuevaSal(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun resumen(contrasena: String, sal: String): String {
        val bytesSal = Base64.decode(sal, Base64.NO_WRAP)
        val especificacion = PBEKeySpec(contrasena.toCharArray(), bytesSal, 120_000, 256)
        return try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(especificacion).encoded
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally {
            especificacion.clearPassword()
        }
    }
}
