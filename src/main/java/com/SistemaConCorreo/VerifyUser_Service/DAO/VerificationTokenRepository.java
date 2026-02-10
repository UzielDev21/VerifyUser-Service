package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import com.SistemaConCorreo.VerifyUser_Service.JPA.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    VerificationToken findByToken(String token);

    VerificationToken findByUsuario(Usuario usuario);
}
