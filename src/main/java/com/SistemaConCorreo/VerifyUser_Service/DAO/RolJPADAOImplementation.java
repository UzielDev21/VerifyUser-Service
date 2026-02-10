package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.RolJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RolJPADAOImplementation implements IRolJPA {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Result GetAll() {

        Result result = new Result();

        try {

            TypedQuery<RolJPA> rolesJPA = entityManager.createQuery(
                    "FROM RolJPA",
                    RolJPA.class);
            List<RolJPA> roles = rolesJPA.getResultList();

            result.objects = (List<Object>) (List<?>) roles;
            result.correct = true;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
            result.status = 500;
        }
        return result;
    }

}
