package com.nowcoder.seckill.common;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

public class Toolbox implements ErrorCode {

    private static final String salt = "你对Lw中有哪些特别的回忆";

    public static String md5(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new BusinessException(PARAMETER_ERROR, "参数不合法！");
        }

        return DigestUtils.md5DigestAsHex((str + salt).getBytes());
    }

    public static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

}
