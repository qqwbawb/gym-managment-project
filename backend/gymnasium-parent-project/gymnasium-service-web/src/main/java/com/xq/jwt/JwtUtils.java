package com.xq.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * jwt工具类
 */
@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtUtils {

    //颁发者
    private String issuer;
    //密钥
    private String secret;
    //过期时间
    private int expiration;

    public String generateToken(Map<String, String> map) {
        //设置令牌过期时间
        Calendar instance = Calendar.getInstance();
        //设置失败时间
        instance.add(Calendar.MINUTE, expiration);
        //创建JWT Builder
        JWTCreator.Builder builder = JWT.create();
        //设置载荷(payload)
        map.forEach((k,v)->{
            builder.withClaim(k,v);
        });
        //生成token
        String token = builder.withIssuer(issuer)
                .withIssuedAt(new Date())
                .withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(secret));
        return token;
    }

    public boolean verifyToken(String token) {
        try {
            JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        } catch (JWTVerificationException e) {
            return false;
        }catch (IllegalArgumentException e){
            return false;
        }
        return true;
    }

    /**
     * 解析token
     * @param token
     * @return
     */
    public DecodedJWT jwtDecoded(String token) {
        try{
            return JWT.require(Algorithm.HMAC256(secret)).build().verify(token);
        }catch (SignatureVerificationException e){
            throw  new RuntimeException("token签名错误");
        }catch (AlgorithmMismatchException e){
            throw  new RuntimeException("token算法不匹配");
        }catch (TokenExpiredException e){
            throw  new RuntimeException("token过期");
        }catch (Exception e){
            throw  new RuntimeException("token解析失败");
        }
    }
}
