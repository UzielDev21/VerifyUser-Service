package com.SistemaConCorreo.VerifyUser_Service.DAO;

public interface ITokenInvalidation {

    void invalidateToken(String jti);

    boolean isTokenInvalid(String jti);

}
