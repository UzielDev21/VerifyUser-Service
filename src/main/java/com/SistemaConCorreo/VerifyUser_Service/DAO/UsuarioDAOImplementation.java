package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAOImplementation implements IUsuarioDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Result Add(Usuario usuario) {
        Result result = new Result();

        try {
            // Persistimos el usuario (INSERT)
            entityManager.persist(usuario);

            // El objeto 'usuario' ya tiene su ID generado automáticamente.
            result.correct = true;
            result.errorMessage = "Usuario registrado correctamente";
            result.object = usuario; // Retornamos el usuario con ID
            result.status = 200;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }

        return result;
    }
}
