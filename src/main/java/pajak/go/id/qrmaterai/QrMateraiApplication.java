package pajak.go.id.qrmaterai;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;


@SpringBootApplication(exclude= {UserDetailsServiceAutoConfiguration.class, DataSourceAutoConfiguration.class})// to remove default security password
@OpenAPIDefinition(info = @Info(title = "QR Materai API Docs", version = "1.0", description = "Documentation QR Materai APIs v1.0"))

public class QrMateraiApplication {

	public static void main(String[] args) {
		SpringApplication.run(QrMateraiApplication.class, args);
	}

}