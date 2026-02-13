package com.SistemaConCorreo.VerifyUser_Service.RestController;

import com.SistemaConCorreo.VerifyUser_Service.DAO.IUsuarioRepositoryDAO;
import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import com.SistemaConCorreo.VerifyUser_Service.Service.JwtService;
import com.SistemaConCorreo.VerifyUser_Service.Service.TokenBlackListService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final IUsuarioRepositoryDAO iUsuarioRepositoryDAO;
    private final TokenBlackListService tokenBlackListService;
    private final JwtService jwtService;

    public AuthRestController(AuthenticationManager authenticationManager,
            IUsuarioRepositoryDAO iUsuarioRepositoryDAO,
            TokenBlackListService tokenBlackListService,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.iUsuarioRepositoryDAO = iUsuarioRepositoryDAO;
        this.tokenBlackListService = tokenBlackListService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity Login(@RequestBody Map<String, String> json) {

        Result result = new Result();

        try {

            String username = json.get("username");
            String password = json.get("password");

            UsuarioJPA usuarioJPA = iUsuarioRepositoryDAO.findByUsername(username);

            if (usuarioJPA == null) {
                result.correct = false;
                result.errorMessage = "Credenciales inexistentes, registrate o valida tus datos correctamente";
                result.status = 401;
                return ResponseEntity.status(result.status).body(result);
            }

            UsernamePasswordAuthenticationToken authInput = new UsernamePasswordAuthenticationToken(
                    username,
                    password);

            try {
                authenticationManager.authenticate(authInput);
            } catch (AuthenticationException ex) {
                result.correct = false;
                result.errorMessage = ex.getLocalizedMessage();
                result.ex = ex;
                result.status = 401;
                return ResponseEntity.status(result.status).body(result);
            }

            String rol = usuarioJPA.rolJPA.getNombreRol();
            int idUsuario = usuarioJPA.getIdUsuario();

            String jwt = jwtService.GenerateUserToken(username, idUsuario, rol);
            result.correct = true;
            result.status = 200;
            result.object = jwt;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return ResponseEntity.status(result.status).body(result);
    }

    @PostMapping("/logout")
    public ResponseEntity Logout(HttpServletRequest servletRequest) {

        Result result = new Result();

        try {

            String authHeader = servletRequest.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                result.correct = false;
                result.errorMessage = "No se pudo obtener el token";
                result.status = 400;
                return ResponseEntity.status(result.status).body(result);
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                result.correct = false;
                result.errorMessage = "Token invalido o expirado";
                result.status = 401;
                return ResponseEntity.status(result.status).body(result);
            }

            String jti = jwtService.GetJtiFromToken(token);
            tokenBlackListService.invalidateToken(jti);
            SecurityContextHolder.clearContext();

            result.correct = true;
            result.status = 200;
            result.object = "Logout exitoso";

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return ResponseEntity.status(result.status).body(result);
    }

}
