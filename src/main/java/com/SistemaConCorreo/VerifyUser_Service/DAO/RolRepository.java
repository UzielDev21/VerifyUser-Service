package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);
}
