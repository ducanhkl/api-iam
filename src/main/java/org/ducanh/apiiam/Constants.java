package org.ducanh.apiiam;

import lombok.experimental.UtilityClass;
import org.ducanh.apiiam.entities.PasswordAlg;

@UtilityClass
public class Constants {
    public static final PasswordAlg DEFAULT_PASSWORD_ALG = PasswordAlg.BCRYPT;
    public static final String DEFAULT_ISSUER = "ducanh";
    public static final String TOKEN_TYPE_NAME = "token_type";
    public static final String NAMESPACE_NAME = "namespace";
    public static final String EMAIL_NAME = "email";
    public static final String TOKEN_TYPE = "token_type";

}
