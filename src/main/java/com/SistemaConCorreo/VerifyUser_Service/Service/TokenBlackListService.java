package com.SistemaConCorreo.VerifyUser_Service.Service;

import com.SistemaConCorreo.VerifyUser_Service.DAO.ITokenInvalidation;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class TokenBlackListService implements ITokenInvalidation {

    private final Set<String> listaNegra = ConcurrentHashMap.newKeySet();

    @Override
    public void invalidateToken(String jti) {
        listaNegra.add(jti);
        System.out.println("el Token se invalido correctamente" + jti);
    }

    @Override
    public boolean isTokenInvalid(String jti) {
        return listaNegra.contains(jti);
    }

}
