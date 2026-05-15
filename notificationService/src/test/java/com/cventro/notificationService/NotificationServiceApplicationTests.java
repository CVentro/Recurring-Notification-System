package com.cventro.notificationService;

import com.cventro.notificationService.configuration.AwsSecretsManagerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest(properties = "logging.file.name=target/notificationService-test.log")
class NotificationServiceApplicationTests {

	@MockBean
	private AwsSecretsManagerConfig awsSecretsManagerConfig;

	@MockBean
	private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

}
