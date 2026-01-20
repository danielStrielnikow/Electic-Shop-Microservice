package pl.electricshop.product_service;

import net.devh.boot.grpc.server.autoconfigure.GrpcServerSecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        GrpcServerSecurityAutoConfiguration.class
})
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
