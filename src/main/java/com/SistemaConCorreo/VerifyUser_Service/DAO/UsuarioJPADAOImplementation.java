package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioJPADAOImplementation implements IUsuarioJPA {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Result GetAll() {

        Result result = new Result();

        try {

            TypedQuery<UsuarioJPA> queryUsuario = entityManager.createQuery(
                    "FROM UsuarioJPA",
                    UsuarioJPA.class);
            List<UsuarioJPA> usuariosJPA = queryUsuario.getResultList();

            result.objects = (List<Object>) (List<?>) usuariosJPA;
            result.correct = true;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

    @Override
    @Transactional
    public Result Add(UsuarioJPA usuarioJPA) {
        
        Result result = new Result();

        try {

            TypedQuery<UsuarioJPA> queryUsuario = entityManager.createQuery(
                    "FROM UsuarioJPA usuarioJPA WHERE usuarioJPA.username = :username",
                    UsuarioJPA.class).setParameter("username", usuarioJPA.getUsername());

            List<UsuarioJPA> usuarios = queryUsuario.getResultList();

            if (!usuarios.isEmpty()) {
                throw new EntityExistsException("El Username" + usuarioJPA.getUsername() + "ya existen en la base de datos");
            }

            String passwordPlano = usuarioJPA.getPassword();
            String passwordEncriptado = passwordEncoder.encode(passwordPlano);
            usuarioJPA.setPassword(passwordEncriptado);

            entityManager.persist(usuarioJPA);
            result.correct = true;
            result.status = 201;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return result;
    }

    @Override
    public Result VerifyUSer(int idUsuario) {

        Result result = new Result();

        try {

            UsuarioJPA usuarioJPA = entityManager.find(
                    UsuarioJPA.class,
                    idUsuario);

            if (usuarioJPA == null) {
                result.correct = false;
                result.errorMessage = "Usuario no encontrado";
                result.status = 404;
                return result;
            }

            usuarioJPA.setIsEnabled(1);
            entityManager.merge(usuarioJPA);
            result.correct = true;
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
