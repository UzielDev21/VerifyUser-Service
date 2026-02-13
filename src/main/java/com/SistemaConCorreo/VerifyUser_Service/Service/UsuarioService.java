package com.SistemaConCorreo.VerifyUser_Service.Service;

import com.SistemaConCorreo.VerifyUser_Service.DAO.IUsuarioRepositoryDAO;
import com.SistemaConCorreo.VerifyUser_Service.DAO.UsuarioJPADAOImplementation;
import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;
import com.SistemaConCorreo.VerifyUser_Service.Producer.VerificationEmailProducer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioJPADAOImplementation usuarioJPADAOImplementation;
    private final EmailVerificationTokenService emailVerificationTokenService;
    private final VerificationEmailProducer verificationEmailProducer;
    private final IUsuarioRepositoryDAO iUsuarioRepositoryDAO;

    public UsuarioService(
            UsuarioJPADAOImplementation usuarioJPADAOImplementation,
            EmailVerificationTokenService emailVerificationTokenService,
            VerificationEmailProducer verificationEmailProducer,
            IUsuarioRepositoryDAO iUsuarioRepositoryDAO) {

        this.usuarioJPADAOImplementation = usuarioJPADAOImplementation;
        this.emailVerificationTokenService = emailVerificationTokenService;
        this.verificationEmailProducer = verificationEmailProducer;
        this.iUsuarioRepositoryDAO = iUsuarioRepositoryDAO;
    }

    @Transactional
    public Result registrarUsuario(UsuarioJPA usuarioJPA) {

        Result result = new Result();

        try {

            result = usuarioJPADAOImplementation.Add(usuarioJPA);

            if (!result.correct) {
                return result;
            }

            int idUsuario = usuarioJPA.getIdUsuario();
            String token = emailVerificationTokenService.GenerateToken(idUsuario);

            verificationEmailProducer.SenderVerificationEmail(
                    usuarioJPA.getEmail(),
                    usuarioJPA.getNombre(),
                    token);

            result.correct = true;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

}
