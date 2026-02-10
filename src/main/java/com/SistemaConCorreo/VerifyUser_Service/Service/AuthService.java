package com.SistemaConCorreo.VerifyUser_Service.Service;

import com.SistemaConCorreo.VerifyUser_Service.DAO.IUsuarioDAO;
import com.SistemaConCorreo.VerifyUser_Service.DAO.RolRepository;
import com.SistemaConCorreo.VerifyUser_Service.DAO.UsuarioRepository;
import com.SistemaConCorreo.VerifyUser_Service.DAO.VerificationTokenRepository;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import com.SistemaConCorreo.VerifyUser_Service.JPA.Rol;
import com.SistemaConCorreo.VerifyUser_Service.JPA.VerificationToken;
import com.SistemaConCorreo.VerifyUser_Service.Model.LoginRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository; // Lo mantenemos solo para consultas (findByEmail)

    @Autowired
    private IUsuarioDAO usuarioDAO; // INYECCIÓN DEL NUEVO DAO

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsJPAService userDetailsService;

    @Autowired
    private JwtService jwtService;

    // LOGIN (Sin cambios)
    public Result login(LoginRequest request) {
        Result result = new Result();
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
            String token = jwtService.generateToken(userDetails);

            result.correct = true;
            result.object = token;
            result.status = 200;
        } catch (Exception e) {
            result.correct = false;
            result.errorMessage = "Credenciales inválidas o cuenta no verificada.";
            result.status = 403;
        }
        return result;
    }

    // REGISTRO (ACTUALIZADO CON DAO)
    public Result registrarUsuario(Usuario usuario) {
        Result result = new Result();
        try {
            // 1. Validaciones previas (Negocio)
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                result.correct = false;
                result.errorMessage = "El email ya está registrado.";
                result.status = 400;
                return result;
            }

            Rol rolUser = rolRepository.findByNombre("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));

            usuario.setRol(rolUser);
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
            usuario.setEnabled(false);

            // 2. INSERCIÓN USANDO TU DAO
            Result resultDAO = usuarioDAO.Add(usuario);

            // Si el DAO falló, retornamos el error del DAO
            if (!resultDAO.correct) {
                return resultDAO;
            }

            // Recuperamos el usuario guardado (que ya tiene ID) del objeto Result
            Usuario guardado = (Usuario) resultDAO.object;

            // 3. Generación de Token y Correo (Lógica posterior al guardado)
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = new VerificationToken(token, guardado);
            tokenRepository.save(verificationToken);

            enviarCorreoVerificacion(guardado.getEmail(), token);

            result.correct = true;
            result.object = "Usuario registrado correctamente. Verifica tu correo.";
            result.status = 200;

        } catch (Exception e) {
            result.correct = false;
            result.errorMessage = e.getMessage();
            result.ex = e;
            result.status = 500;
        }
        return result;
    }

    // VERIFICACION (Sin cambios)
    public Result verificarCuenta(String token) {
        Result result = new Result();
        try {
            VerificationToken vt = tokenRepository.findByToken(token);
            if (vt == null) {
                result.correct = false;
                result.errorMessage = "Token inválido";
                result.status = 400;
                return result;
            }

            if (vt.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                result.correct = false;
                result.errorMessage = "Token expirado";
                result.status = 400;
                return result;
            }

            Usuario usuario = vt.getUsuario();
            usuario.setEnabled(true);

            // Aquí podrías usar otro método del DAO si quisieras (ej. Update), 
            // pero repository.save también funciona para updates simples.
            usuarioRepository.save(usuario);

            tokenRepository.delete(vt);

            result.correct = true;
            result.object = "Cuenta verificada con éxito.";
            result.status = 200;

        } catch (Exception e) {
            result.correct = false;
            result.errorMessage = e.getMessage();
            result.status = 500;
        }
        return result;
    }

    private void enviarCorreoVerificacion(String email, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(email);
        helper.setSubject("Verifica tu cuenta");
        String url = "http://localhost:8080/api/auth/verify?token=" + token;

        String htmlContent = "<h1>Bienvenido</h1>"
                + "<p>Haz clic para activar tu cuenta:</p>"
                + "<a href='" + url + "'>VERIFICAR AHORA</a>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
    }
}
