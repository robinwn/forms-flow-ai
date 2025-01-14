package org.camunda.bpm.extension.hooks.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import net.minidev.json.JSONArray;
import org.camunda.bpm.extension.commons.connector.HTTPServiceInvoker;
import org.camunda.bpm.extension.hooks.controllers.data.Authorization;
import org.camunda.bpm.extension.hooks.controllers.mapper.AuthorizationMapper;
import org.camunda.bpm.extension.hooks.controllers.stubs.AuthorizationStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test class for AdminController
 */
@ExtendWith(SpringExtension.class)
public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @Mock
    private Authentication auth;

    @Mock
    private NamedParameterJdbcTemplate bpmJdbcTemplate;

    @Mock
    private HTTPServiceInvoker httpServiceInvoker;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        SecurityContextHolder.getContext().setAuthentication(auth);

        Map<String, Object> claims = new HashMap<>();
        JSONArray groups = new JSONArray();
        groups.add(new String("/camunda-admin"));
        groups.add(new String("/formsflow/formsflow-reviewer"));
        claims.put("groups", groups);

        OidcUser oidcUser = mock(OidcUser.class);
        when(auth.getPrincipal())
                .thenReturn(oidcUser);

        when(oidcUser.getClaims())
                .thenReturn(claims);

        List<Authorization> authorizationList = new ArrayList<>();
        authorizationList.add(new AuthorizationStub("test-id-1","test-id-1","224233456456"));
        given(bpmJdbcTemplate.query(anyString(), any(SqlParameterSource.class), any(AuthorizationMapper.class)))
                .willReturn(authorizationList);
    }
    
    /**
     * This test case perform a positive test over getForms with admin group name
     * Expect Status OK and content
     */
	@Test
	public void getFormsSuccess_with_adminGroupName() throws Exception {
		final String adminGroupName = "camunda-admin";
        ReflectionTestUtils.setField(adminController, "adminGroupName", adminGroupName);
        when(httpServiceInvoker.execute(any(), any(HttpMethod.class), any()))
                .thenReturn(ResponseEntity.ok("{\"totalCount\":\"2\",\"forms\":[" +
                        "{\"formId\":\"foi\",\"formName\":\"Freedom Of Information\",\"processKey\":\"224233456456\"}," +
                        "{\"formId\":\"nbl\",\"formName\":\"New Business Licence\",\"processKey\":\"456456456\"}]}"));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/engine-rest-ext/form"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"formId\":\"foi\",\"formName\":\"Freedom Of Information\",\"processKey\":\"224233456456\"},{\"formId\":\"nbl\",\"formName\":\"New Business Licence\",\"processKey\":\"456456456\"}]"));
    }

	/**
     * This test case perform a positive test over getForms without admin group name
     * Expect Status OK and content
     */
    @Test
    public void getFormsSuccess_without_adminGroupName() throws Exception { 	
        when(httpServiceInvoker.execute(any(), any(HttpMethod.class), any()))
                .thenReturn(ResponseEntity.ok("{\"totalCount\":\"2\",\"forms\":[" +
                        "{\"formId\":\"foi\",\"formName\":\"Freedom Of Information\",\"processKey\":\"224233456456\"}," +
                        "{\"formId\":\"nbl\",\"formName\":\"New Business Licence\",\"processKey\":\"456456456\"}]}"));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/engine-rest-ext/form"))
                .andExpect(status().isOk())
                .andExpect(content().string("[{\"formId\":\"foi\",\"formName\":\"Freedom Of Information\",\"processKey\":\"224233456456\"}]"));
    }

	/**
	 * Expect Status OK and empty content
	 */
	@Test
    public void getFormsFailure() throws Exception {
        when(httpServiceInvoker.execute(any(), any(HttpMethod.class), any()))
                .thenReturn(ResponseEntity.ok("{\"totalCount\":\"2\",\"forms\":[" +
                        "{\"formId\":\"foi\",\"formName\":\"Freedom Of Information\",\"processKey\":\"224233456456\"}," +
                        "{\"formId\":\"nbl\",\"formName\":\"New Business Licence\",\"processKey\":\"456456456\"]}"));
        mockMvc.perform(
                MockMvcRequestBuilders.get("/engine-rest-ext/form"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }
}
