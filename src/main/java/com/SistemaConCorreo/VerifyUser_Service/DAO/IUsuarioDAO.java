package com.SistemaConCorreo.VerifyUser_Service.DAO;

import com.SistemaConCorreo.VerifyUser_Service.JPA.Usuario;
import com.SistemaConCorreo.VerifyUser_Service.Model.Result;

public interface IUsuarioDAO {

    Result Add(Usuario usuario);

}
