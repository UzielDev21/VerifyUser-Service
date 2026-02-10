package com.SistemaConCorreo.VerifyUser_Service.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class Result {

    public boolean correct;
    public String errorMessage;
    public Object object;
    public List<Object> objects;
    public Exception ex;

    @JsonIgnore
    public int status;

}
