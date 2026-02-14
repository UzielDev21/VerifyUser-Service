package com.SistemaConCorreo.VerifyUser_Service.RestController;

import com.SistemaConCorreo.VerifyUser_Service.DAO.UsuarioJPADAOImplementation;
import com.SistemaConCorreo.VerifyUser_Service.JPA.RolJPA;
import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import com.SistemaConCorreo.VerifyUser_Service.Service.EmailVerificationTokenService;
import com.SistemaConCorreo.VerifyUser_Service.Service.UsuarioService;
import jakarta.persistence.EntityExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class UsuarioRestController {

    @Autowired
    UsuarioJPADAOImplementation usuarioJPADAOImplementation;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailVerificationTokenService emailVerificationTokenService;

    @GetMapping("/usuarios")
    public ResponseEntity GetAll() {

        Result result = new Result();

        try {
            result = usuarioJPADAOImplementation.GetAll();
            result.correct = true;
            result.status = 200;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return ResponseEntity.status(result.status).body(result);
    }

    @PostMapping("/usuario")
    public ResponseEntity AddUsuario(@RequestBody UsuarioJPA usuarioJPA) {

        Result result = new Result();

        try {
            
            if (usuarioJPA.rolJPA == null) {
                RolJPA rolJPA = new RolJPA();
                rolJPA.setIdRol(1);
                usuarioJPA.setRolJPA(rolJPA);
            }
            
            result = usuarioService.registrarUsuario(usuarioJPA);

            if (result.status == 0) {

                if (result.correct) {

                    result.status = 201;

                } else {

                    if (result.ex instanceof EntityExistsException) {
                        result.status = 409;
                    } else {
                        result.status = 500;
                    }
                }
            }
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return ResponseEntity.status(result.status).body(result);
    }

    @GetMapping("/auth/verify")
    public ResponseEntity VerifyAccount(@RequestParam("tokenEmail") String tokenEmail) {

        Result result = new Result();

        if (!emailVerificationTokenService.IsTokenValid(tokenEmail)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token Invalido");
        }

        Integer idUsuario = emailVerificationTokenService.GetUserIdFromToken(tokenEmail);

        if (idUsuario == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El token no esta asociado al usuario");
        }

        result = usuarioJPADAOImplementation.VerifyUSer(idUsuario);
        if (!result.correct) {
            return ResponseEntity.status(result.status).body(result.errorMessage);
        }

        emailVerificationTokenService.MarkTokenAsUsed(tokenEmail);

        return ResponseEntity.ok("La cuenta se verifico correctmente, ya puedes iniciar sesión");
    }

}
