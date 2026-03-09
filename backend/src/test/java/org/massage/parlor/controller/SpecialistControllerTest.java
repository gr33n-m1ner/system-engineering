package org.massage.parlor.controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.UpdateSpecialistRequest;
import org.massage.parlor.model.Specialist;
import org.massage.parlor.service.SpecialistService;
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
class SpecialistControllerTest {

    private static final Integer ID_1 = 1;
    private static final Integer EXPERIENCE = 5;
    private static final String ROLE_SPECIALIST = "SPECIALIST";
    private static final String ROLE_CLIENT = "CLIENT";
    private static final String ROLE_ADMIN = "ADMIN";
    private static final Specialist SPECIALIST = createSpecialist(ID_1, EXPERIENCE);
    private static final UpdateSpecialistRequest UPDATE_SPECIALIST_REQUEST = new UpdateSpecialistRequest(EXPERIENCE);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SpecialistService specialistService;

    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowAuthenticatedUserToGetSpecialistById() throws Exception {
        when(specialistService.getSpecialistById(ID_1)).thenReturn(Optional.of(SPECIALIST));

        mockMvc.perform(get("/api/specialists/" + ID_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_1))
                .andExpect(jsonPath("$.experience").value(EXPERIENCE));
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldAllowAuthenticatedUserToGetAllSpecialists() throws Exception {
        Specialist specialist2 = createSpecialist(2, 10);

        when(specialistService.getAllSpecialists()).thenReturn(List.of(SPECIALIST, specialist2));

        mockMvc.perform(get("/api/specialists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToUpdateOwnProfile() throws Exception {
        Specialist updatedSpecialist = createSpecialist(ID_1, 10);

        when(specialistService.updateSpecialist(eq(ID_1), any(UpdateSpecialistRequest.class)))
                .thenReturn(updatedSpecialist);

        mockMvc.perform(put("/api/specialists/" + ID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UPDATE_SPECIALIST_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experience").value(10));
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_CLIENT)
    void shouldDenyClientToUpdateOtherSpecialistProfile() throws Exception {

        mockMvc.perform(put("/api/specialists/" + ID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UPDATE_SPECIALIST_REQUEST)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "3", roles = ROLE_ADMIN)
    void shouldAllowAdminToUpdateAnySpecialistProfile() throws Exception {
        Specialist updatedSpecialist = createSpecialist(ID_1, 15);

        when(specialistService.updateSpecialist(eq(ID_1), any(UpdateSpecialistRequest.class)))
                .thenReturn(updatedSpecialist);

        mockMvc.perform(put("/api/specialists/" + ID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(UPDATE_SPECIALIST_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.experience").value(15));
    }

    private static Specialist createSpecialist(Integer id, Integer experience) {
        Specialist specialist = new Specialist();
        specialist.setId(id);
        specialist.setExperience(experience);
        specialist.setActive(true);
        return specialist;
    }
}
