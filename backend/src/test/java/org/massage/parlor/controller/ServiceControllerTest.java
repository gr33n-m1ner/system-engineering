package org.massage.parlor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.massage.parlor.dto.AddSpecialistOfferingRequest;
import org.massage.parlor.dto.CreateServiceCatalogRequest;
import org.massage.parlor.model.ServiceCatalog;
import org.massage.parlor.model.SpecialistOffering;
import org.massage.parlor.model.User;
import org.massage.parlor.service.ServiceCatalogService;
import org.massage.parlor.service.SpecialistOfferingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ServiceControllerTest {

    private static final Integer ID = 1;
    private static final Integer SPECIALIST_ID = 1;
    private static final String TITLE = "massage";
    private static final BigDecimal PRICE = new BigDecimal("50.00");
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_SPECIALIST = "SPECIALIST";
    private static final String ROLE_CLIENT = "CLIENT";
    private static final AddSpecialistOfferingRequest ADD_SPECIALIST_OFFERING_REQUEST =
            new AddSpecialistOfferingRequest(1, PRICE);
    private static final ServiceCatalog SERVICE_CATALOG = new ServiceCatalog(ID, TITLE, true);
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private ServiceCatalogService serviceCatalogService;
    
    @MockBean
    private SpecialistOfferingService specialistOfferingService;
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowAuthenticatedUserToGetAllServiceCatalogs() throws Exception {
        ServiceCatalog service2 = new ServiceCatalog(2, "massage2", true);
        
        when(serviceCatalogService.getAllServiceCatalogs()).thenReturn(List.of(SERVICE_CATALOG, service2));
        
        mockMvc.perform(get("/api/services/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowAuthenticatedUserToGetServiceCatalogById() throws Exception {

        when(serviceCatalogService.getServiceCatalogById(ID)).thenReturn(Optional.of(SERVICE_CATALOG));
        
        mockMvc.perform(get("/api/services/types/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TITLE));
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_ADMIN)
    void shouldAllowAdminToCreateServiceCatalog() throws Exception {
        CreateServiceCatalogRequest request = new CreateServiceCatalogRequest(TITLE);
        
        when(serviceCatalogService.createServiceCatalog(any(CreateServiceCatalogRequest.class))).thenReturn(SERVICE_CATALOG);
        
        mockMvc.perform(post("/api/services/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(TITLE));
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldDenyClientToCreateServiceCatalog() throws Exception {
        CreateServiceCatalogRequest request = new CreateServiceCatalogRequest(TITLE);
        
        mockMvc.perform(post("/api/services/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldAllowAuthenticatedUserToGetSpecialistOfferings() throws Exception {
        SpecialistOffering offering1 = new SpecialistOffering();
        SpecialistOffering offering2 = new SpecialistOffering();
        
        when(specialistOfferingService.getOfferingsBySpecialistId(SPECIALIST_ID))
                .thenReturn(List.of(offering1, offering2));
        
        mockMvc.perform(get("/api/services/specialists/" + SPECIALIST_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToAddOfferingToOwnProfile() throws Exception {
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setPrice(PRICE);

        when(specialistOfferingService.addOfferingToSpecialist(eq(SPECIALIST_ID), any(AddSpecialistOfferingRequest.class)))
                .thenReturn(specialistOffering);
        
        mockMvc.perform(post("/api/services/specialists/" + SPECIALIST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ADD_SPECIALIST_OFFERING_REQUEST)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(50.0));
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_CLIENT)
    void shouldDenyClientToAddOfferingToOtherSpecialist() throws Exception {

        mockMvc.perform(post("/api/services/specialists/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ADD_SPECIALIST_OFFERING_REQUEST)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToAddOfferingToOtherSpecialist() throws Exception {

        mockMvc.perform(post("/api/services/specialists/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ADD_SPECIALIST_OFFERING_REQUEST)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldReturnBadRequestWhenAddingDuplicateOffering() throws Exception {

        when(specialistOfferingService.addOfferingToSpecialist(eq(SPECIALIST_ID), any(AddSpecialistOfferingRequest.class)))
                .thenThrow(new DataIntegrityViolationException(""));
        
        mockMvc.perform(post("/api/services/specialists/" + SPECIALIST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ADD_SPECIALIST_OFFERING_REQUEST)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_ADMIN)
    void shouldAllowAdminToDeactivateServiceCatalog() throws Exception {
        mockMvc.perform(delete("/api/services/types/" + ID))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToDeactivateServiceCatalog() throws Exception {
        mockMvc.perform(delete("/api/services/types/" + ID))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldAllowSpecialistToDeactivateOwnOffering() throws Exception {
        SpecialistOffering specialistOffering = createSpecialistOffering(SPECIALIST_ID);
        
        when(specialistOfferingService.getSpecialistOfferingById(ID)).thenReturn(Optional.of(specialistOffering));
        
        mockMvc.perform(delete("/api/services/specialist-services/" + ID))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(username = "1", roles = ROLE_SPECIALIST)
    void shouldDenySpecialistToDeactivateOtherSpecialistOffering() throws Exception {
        SpecialistOffering specialistOffering = createSpecialistOffering(2);
        
        when(specialistOfferingService.getSpecialistOfferingById(ID)).thenReturn(Optional.of(specialistOffering));
        
        mockMvc.perform(delete("/api/services/specialist-services/" + ID))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(username = "3", roles = ROLE_ADMIN)
    void shouldAllowAdminToDeactivateAnySpecialistOffering() throws Exception {
        SpecialistOffering specialistOffering = createSpecialistOffering(SPECIALIST_ID);
        
        when(specialistOfferingService.getSpecialistOfferingById(ID)).thenReturn(Optional.of(specialistOffering));
        
        mockMvc.perform(delete("/api/services/specialist-services/" + ID))
                .andExpect(status().isOk());
    }
    
    private SpecialistOffering createSpecialistOffering(Integer specialistId) {
        SpecialistOffering specialistOffering = new SpecialistOffering();
        specialistOffering.setId(ServiceControllerTest.ID);
        specialistOffering.setActive(true);
        
        User user = new User();
        user.setId(specialistId);
        
        org.massage.parlor.model.Specialist specialist = new org.massage.parlor.model.Specialist();
        specialist.setId(specialistId);
        specialist.setUser(user);
        
        specialistOffering.setSpecialist(specialist);
        return specialistOffering;
    }
}
