package com.SistemaConCorreo.VerifyUser_Service.Service;

import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.SistemaConCorreo.VerifyUser_Service.DAO.IUsuarioRepositoryDAO;

@Service
public class UserDetailsJPAService implements UserDetailsService {

    private final IUsuarioRepositoryDAO iUsuarioRepository;

    public UserDetailsJPAService(IUsuarioRepositoryDAO iUsuarioRepository) {
        this.iUsuarioRepository = iUsuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsuarioJPA usuario = iUsuarioRepository.findByUsername(username);

        if (usuario.getIsEnabled() == 0) {
            throw new UsernameNotFoundException("El usuario no ha verificado su correo.");
        }

        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado" + username);
        }

        int userValid = usuario.getIsEnabled();
        boolean isDisable = (userValid == 0);

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.rolJPA.getNombreRol())
                .disabled(isDisable)
                .build();
    }
}
