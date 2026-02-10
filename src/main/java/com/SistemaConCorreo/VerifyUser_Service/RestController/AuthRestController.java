package com.SistemaConCorreo.VerifyUser_Service.RestController;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import com.SistemaConCorreo.VerifyUser_Service.Model.LoginRequest;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import com.SistemaConCorreo.VerifyUser_Service.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest loginRequest) {
        Result result = authService.login(loginRequest);
        return ResponseEntity.status(result.status).body(result);
    }

    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody Usuario usuario) {
        Result result = authService.registrarUsuario(usuario);
        return ResponseEntity.status(result.status).body(result);
    }

    @GetMapping("/verify")
    public ResponseEntity<Result> verifyAccount(@RequestParam("token") String token) {
        Result result = authService.verificarCuenta(token);
        return ResponseEntity.status(result.status).body(result);
    }
}
