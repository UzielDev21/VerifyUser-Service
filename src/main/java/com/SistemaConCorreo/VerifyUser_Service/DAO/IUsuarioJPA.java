package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.UsuarioJPA;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;

public interface IUsuarioJPA {
    
    Result GetAll();
    
    Result Add(UsuarioJPA usuarioJPA);
    
    Result VerifyUSer(int idUsuario);
    

}
