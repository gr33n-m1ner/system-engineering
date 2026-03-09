package org.massage.parlor.controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.UpdateRoleRequest;
import org.massage.parlor.dto.UpdateUserRequest;
import org.massage.parlor.model.Role;
import org.massage.parlor.model.User;
import org.massage.parlor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    private static final String ADMIN = "admin";
    private static final String SPECIALIST = "specialist";
    private static final String LOGIN = "testuser";
    private static final Integer ID_1 = 1;
    private static final Integer ID_2 = 2;
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_SPECIALIST = "SPECIALIST";
    private static final String ROLE_CLIENT = "CLIENT";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldDenyAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_ADMIN)
    void shouldAllowAdminToGetAllUsers() throws Exception {
        User user1 = createUser(ID_1, "user1", Role.CLIENT);
        User user2 = createUser(ID_2, "user2", Role.CLIENT);

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldDenyClientToGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowClientToAccessOwnProfile() throws Exception {
        User currentUser = createUser(ID_1, LOGIN, Role.CLIENT);
        when(userService.getUserById(ID_1)).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/api/users/" + ID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN));
    }

    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToAccessAnyProfile() throws Exception {
        User user = createUser(ID_2, LOGIN, Role.CLIENT);

        when(userService.getUserById(ID_2)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/" + ID_2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN));
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldDenyClientToAccessOtherProfile() throws Exception {
        User otherUser = createUser(ID_2, "otheruser", Role.CLIENT);

        when(userService.getUserById(ID_2)).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/users/" + ID_2))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_ADMIN)
    void shouldAllowAdminToAccessAnyProfile() throws Exception {
        User user = createUser(ID_1, LOGIN, Role.CLIENT);

        when(userService.getUserById(ID_1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/" + ID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN));
    }

    @Test
    @WithMockUser(username = "2", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToGetUserByLogin() throws Exception {
        User specialist = createUser(ID_2, SPECIALIST, Role.SPECIALIST);
        User user = createUser(ID_1, LOGIN, Role.CLIENT);

        when(userService.getUserById(2)).thenReturn(Optional.of(specialist));
        when(userService.getUserByLogin(LOGIN)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/login/" + LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN));
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowClientToGetOwnUserByLogin() throws Exception {
        User currentUser = createUser(ID_1, LOGIN, Role.CLIENT);

        when(userService.getUserById(ID_1)).thenReturn(Optional.of(currentUser));
        when(userService.getUserByLogin(LOGIN)).thenReturn(Optional.of(currentUser));

        mockMvc.perform(get("/api/users/login/" + LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(LOGIN));
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldDenyClientToGetOtherUserByLogin() throws Exception {
        User currentUser = createUser(3, "other_login", Role.CLIENT);
        User otherUser = createUser(ID_2, LOGIN, Role.CLIENT);

        when(userService.getUserById(3)).thenReturn(Optional.of(currentUser));
        when(userService.getUserByLogin(LOGIN)).thenReturn(Optional.of(otherUser));

        mockMvc.perform(get("/api/users/login/" + LOGIN))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowClientToUpdateOwnProfile() throws Exception {
        User updatedUser = createUser(ID_1, LOGIN, Role.CLIENT);
        updatedUser.setName("Updated Name");

        UpdateUserRequest request = new UpdateUserRequest("Updated Name", null, null, null);

        when(userService.updateUser(eq(ID_1), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/" + ID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_ADMIN)
    void shouldAllowAdminToUpdateUserRole() throws Exception {
        User user = createUser(ID_1, LOGIN, Role.CLIENT);
        user.setRole(Role.SPECIALIST);

        UpdateRoleRequest request = new UpdateRoleRequest(Role.SPECIALIST);

        when(userService.updateUserRole(ID_1, Role.SPECIALIST)).thenReturn(user);

        mockMvc.perform(put("/api/users/" + ID_1 + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("SPECIALIST"));
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldDenyClientToUpdateUserRole() throws Exception {
        UpdateRoleRequest request = new UpdateRoleRequest(Role.ADMIN);

        mockMvc.perform(put("/api/users/" + ID_1 + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private User createUser(Integer id, String login, Role role) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setName("name");
        user.setRole(role);
        return user;
    }
}
