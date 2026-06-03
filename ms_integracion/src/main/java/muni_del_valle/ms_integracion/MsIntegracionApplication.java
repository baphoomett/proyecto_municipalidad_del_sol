package muni_del_valle.ms_integracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;

@SpringBootApplication
@EnableRabbit
public class MsIntegracionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsIntegracionApplication.class, args);
    }
}
