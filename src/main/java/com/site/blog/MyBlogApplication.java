package com.site.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 13
 * @qq交流群 796794009
 * @email 2449207463@qq.com
 * @link http://13blog.site
 */
@MapperScan("com.site.blog.dao")
@SpringBootApplication
public class MyBlogApplication {
    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MyBlogApplication.class);
        springApplication.setBannerMode(Banner.Mode.LOG);
        springApplication.run(args);
//        SpringApplication.run(MyBlogApplication.class, args);
    }
}
