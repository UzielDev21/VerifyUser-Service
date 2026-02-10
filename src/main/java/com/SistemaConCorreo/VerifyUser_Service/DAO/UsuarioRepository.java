package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByNombre(String nombre);
}
