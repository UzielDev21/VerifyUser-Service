package com.SistemaConCorreo.VerifyUser_Service.Service;

import com.SistemaConCorreo.VerifyUser_Service.DAO.UsuarioRepository;
import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsJPAService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String emailOrNombre) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByEmail(emailOrNombre)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + emailOrNombre));

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("El usuario no ha verificado su correo.");
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Spring Security espera roles con el prefijo "ROLE_" o autoridades simples
        authorities.add(new SimpleGrantedAuthority(usuario.getRol().getNombre()));

        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                usuario.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}
