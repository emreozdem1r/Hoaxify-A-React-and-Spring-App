package com.hoaxify.hoaxify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.hoaxify.hoaxify.error.ApiError;
import com.hoaxify.hoaxify.user.User;
import com.hoaxify.hoaxify.user.UserRepository;
import com.hoaxify.hoaxify.user.UserService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class LoginControllerTest {

	private static final String API_1_0_LOGIN = "/api/1.0/login";
	
	@Autowired
	TestRestTemplate testRestTemplate;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserService userService;
	
	@Before
	public void cleanup() {
		userRepository.deleteAll();
		testRestTemplate.getRestTemplate().getInterceptors().clear();
	}
	
	@Test
	public void postLogin_withoutUserCredentials_recieveUnautorized() {
	
		ResponseEntity<Object> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test
	public void postLogin_withoutUserCredentials_recieveApiError() {
	
		ResponseEntity<ApiError> response = login(ApiError.class);
		assertThat(response.getBody().getUrl()).isEqualTo(API_1_0_LOGIN);
	}
	@Test
	public void postLogin_withoutUserCredentials_recieveApiErrorWithoutValidationErrors() {
	
		ResponseEntity<String> response = login(String.class);
		assertThat(response.getBody().contains("validationErrors")).isFalse();
	}
	
	@Test
	public void postLogin_withIncorrectCredentials_recieveAuthorizedWithoutWWWAuthenticationHeader() {
		authenticate();
		ResponseEntity<Object> response = login(Object.class);
		assertThat(response.getHeaders().containsKey("WWW-Authenticate")).isFalse();
	}
	@Test
	public void postLogin_withIncorrectCredentials_recieveUnautorized() {
		authenticate();
		ResponseEntity<Object> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}
	@Test
	public void postLogin_withValidCredentials_recieveOk() {
		
		userService.save(TestUtil.createValidUser());
		
		authenticate();
		ResponseEntity<Object> response = login(Object.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
	}
	@Test
	public void postLogin_withValidCredentials_recieveLoggedInUserId() {
		
		User inDB = userService.save(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
		
		Map<String, Object> body = response.getBody();
		
		Integer id = (Integer) body.get("id");
		
		assertThat(id).isEqualTo(inDB.getId());
		
	}
	@Test
	public void postLogin_withValidCredentials_recieveLoggedInUsersImage() {
		
		User inDB = userService.save(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
		
		Map<String, Object> body = response.getBody();
		
		String image = (String) body.get("image");
		
		assertThat(image).isEqualTo(inDB.getImage());
		
	}/*
	@Test
	public void postLogin_withValidCredentials_recieveLoggedInUsersDisplayName() {
		
		User inDB = userService.save(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
		
		Map<String, Object> body = response.getBody();
		
		String displayName = (String) body.get("displayName");
		
		assertThat(displayName).isEqualTo(inDB.getDisplayName());
		
	}*/
	@Test
	public void postLogin_withValidCredentials_recieveLoggedInUsersUserName() {
		
		User inDB = userService.save(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
		
		Map<String, Object> body = response.getBody();
		
		String username = (String) body.get("username");
		
		assertThat(username).isEqualTo(inDB.getUsername());	
	}
/*
	@Test
	public void postLogin_withValidCredentials_notRecieveLoggedInUsersPassword() {
		
		User inDB = userService.save(TestUtil.createValidUser());
		authenticate();
		ResponseEntity<Map<String, Object>> response = login(new ParameterizedTypeReference<Map<String, Object>>() {});
		
		Map<String, Object> body = response.getBody();
		
		assertThat(body.containsKey("password")).isFalse();
	}
	*/
	private void authenticate () {
		testRestTemplate.getRestTemplate().
			getInterceptors().add(new BasicAuthenticationInterceptor("test-user", "P4ssword")); 
	}

	public <T> ResponseEntity<T> login(Class<T> responseType){
		
		return testRestTemplate.postForEntity(API_1_0_LOGIN, null, responseType);
	}
	public <T> ResponseEntity<T> login(ParameterizedTypeReference<T> responseType){
		
		return testRestTemplate.exchange(API_1_0_LOGIN, HttpMethod.POST , null, responseType);
	}
	
	
}
