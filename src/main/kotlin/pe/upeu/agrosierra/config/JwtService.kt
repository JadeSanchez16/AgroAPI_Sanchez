package pe.upeu.agrosierra.config

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jose.crypto.MACVerifier
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import pe.upeu.agrosierra.seguridad.Usuario
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.util.Date

data class JwtIdentity(val usuarioId: Long, val username: String)

@Service
class JwtService(
    @Value("\${agrosierra.jwt.secret}") secret: String,
    @Value("\${agrosierra.jwt.issuer}") private val issuer: String,
    @Value("\${agrosierra.jwt.expiration-seconds}") private val expirationSeconds: Long,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val secretBytes = secret.toByteArray(StandardCharsets.UTF_8).also {
        require(it.size >= 32) { "JWT_SECRET debe tener al menos 32 bytes" }
    }

    fun emitir(usuario: Usuario): String {
        val now = Instant.now(clock)
        val claims = JWTClaimsSet.Builder()
            .issuer(issuer)
            .subject(requireNotNull(usuario.id).toString())
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
            .claim("usuario", usuario.username)
            .claim("rol", usuario.rol.nombre)
            .claim("privilegios", usuario.rol.privilegios.map { it.name }.sorted())
            .build()
        return SignedJWT(JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims)
            .apply { sign(MACSigner(secretBytes)) }
            .serialize()
    }

    fun validar(token: String): JwtIdentity? = runCatching {
        val jwt = SignedJWT.parse(token)
        require(jwt.verify(MACVerifier(secretBytes)))
        val claims = jwt.jwtClaimsSet
        require(claims.issuer == issuer)
        require(claims.expirationTime?.toInstant()?.isAfter(Instant.now(clock)) == true)
        JwtIdentity(claims.subject.toLong(), claims.getStringClaim("usuario"))
    }.getOrNull()
}
