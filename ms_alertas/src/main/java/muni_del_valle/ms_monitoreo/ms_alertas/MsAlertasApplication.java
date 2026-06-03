package muni_del_valle.ms_monitoreo.ms_alertas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRabbit
@EnableScheduling
public class MsAlertasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAlertasApplication.class, args);
	}

}
