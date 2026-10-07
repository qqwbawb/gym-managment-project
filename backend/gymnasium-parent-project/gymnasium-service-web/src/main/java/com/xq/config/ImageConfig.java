package com.xq.config;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class ImageConfig {

    @Bean
    public DefaultKaptcha getDefaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();

        Properties properties = new Properties();
        //设置验证码的边框
        properties.setProperty(Constants.KAPTCHA_BORDER, "yes");
        //设置边框的颜色
        properties.setProperty(Constants.KAPTCHA_BORDER_COLOR, "105,179,90");
        //设置字体的颜色
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_COLOR, "blue");
        //设置图片宽度
        properties.setProperty(Constants.KAPTCHA_IMAGE_WIDTH, "200");
        //设置验证码高度
        properties.setProperty(Constants.KAPTCHA_IMAGE_HEIGHT, "36");
        //生成验证码的字符串
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_STRING,"0123456789");
        //去掉干扰线
        properties.setProperty(Constants.KAPTCHA_NOISE_IMPL,"com.google.code.kaptcha.impl.NoNoise");
        //设置字体大小
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_SIZE,"34");
        //设置字体格式
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_FONT_NAMES,"楷体");
        //设置验证码位数
        properties.setProperty(Constants.KAPTCHA_TEXTPRODUCER_CHAR_LENGTH,"4");
        //图片效果
        properties.setProperty(Constants.KAPTCHA_OBSCURIFICATOR_IMPL,"com.google.code.kaptcha.impl.ShadowGimpy");

        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        return defaultKaptcha;
    }
}
