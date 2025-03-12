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
    public static final String PAGE_NUMBER_HEADER = "X-Page-Number";
    public static final String PAGE_SIZE_HEADER = "X-Page-Size";
    public static final String TOTAL_ELEMENTS_HEADER = "X-Total-Elements";
    public static final String TOTAL_PAGES_HEADER = "X-Total-Pages";
    public static final String THREAD_EXECUTOR = "common-thread-pool";
    public static final String NAMESPACE_CHANGE_TOPIC = "namespace-change";
}
