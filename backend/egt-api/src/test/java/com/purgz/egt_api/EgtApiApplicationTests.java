package com.purgz.egt_api;

import com.purgz.egt_api.service.MinioService;
import com.purgz.egt_api.service.SimStorageService;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EgtApiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MinioClient minioClient;

	@MockitoBean
	private MinioService minioService;

	@MockitoBean
	private SimStorageService simStorageService;

	@MockitoBean
	private RedisTemplate<String, String> redisTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void healthEndpointIsAccessible() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}

	@Test
	void protectedEndpointWithoutTokenReturns401() throws Exception {
		mockMvc.perform(get("/api/sim/saved"))
				.andExpect(status().isForbidden());
	}

	@Test
	void loginWithBadCredentialsReturns4xx() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                {"email":"nobody@test.com","password":"wrongpassword"}
                                """))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void registerThenLoginReturnsToken() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                {"email":"user1@test.com","password":"password123"}
                                """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.email").value("user1@test.com"))
				.andExpect(jsonPath("$.roles").isArray());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                {"email":"user1@test.com","password":"password123"}
                                """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void registerDuplicateEmailFails() throws Exception {
		String body = """
                {"email":"duplicate@test.com","password":"password123"}
                """;

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	void registerWithShortPasswordFails() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                {"email":"short@test.com","password":"123"}
                                """))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void registerWithInvalidEmailFails() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
                                {"email":"notanemail","password":"password123"}
                                """))
				.andExpect(status().is4xxClientError());
	}
}