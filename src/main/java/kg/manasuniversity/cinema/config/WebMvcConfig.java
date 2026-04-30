package kg.manasuniversity.cinema.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@SuppressWarnings("removal")
public class WebMvcConfig implements WebMvcConfigurer {

    // Frontend uses URLs like /movies/, /bookings/{id}/confirm/.
    // Spring Boot 3 disables trailing-slash matching by default — re-enable it.
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}
