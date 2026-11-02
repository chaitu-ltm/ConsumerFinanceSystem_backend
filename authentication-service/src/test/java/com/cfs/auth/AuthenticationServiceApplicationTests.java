package com.cfs.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:cfs-auth-test;DB_CLOSE_DELAY=-1;MODE=MySQL",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"eureka.client.enabled=false",
	"security.jwt.secret-base64=VGhpc0lzQVRlc3RTZWNyZXRLZXlGb3JIUzI1NkJpdHM=",
	"security.jwt.expiration-ms=3600000",
	"security.bootstrap-admin.email=admin@test.local",
	"security.bootstrap-admin.password=TestOnly-Password-123!",
	"security.bootstrap-admin.first-name=Test",
	"security.bootstrap-admin.last-name=Admin"
})
class AuthenticationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
