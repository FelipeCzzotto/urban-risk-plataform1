package br.com.fiap.urbanrisk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.simulador.ativo=false",
        "spring.datasource.url=jdbc:h2:mem:teste"
})
@AutoConfigureMockMvc
class EventoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void sensorSemChaveDeveDar401() throws Exception {
        String json = """
                {"tipo":"ALAGAMENTO","valor":60,"latitude":-23.5,"longitude":-46.6,"bairro":"Ipiranga","fonte":"S1"}
                """;
        mvc.perform(post("/api/sensores").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sensorComValorAltoDeveVirarAlerta() throws Exception {
        String json = """
                {"tipo":"ALAGAMENTO","valor":60,"latitude":-23.5,"longitude":-46.6,"bairro":"Ipiranga","fonte":"S2"}
                """;
        mvc.perform(post("/api/sensores").header("X-API-KEY", "fiap2026")
                        .contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origem").value("SENSOR"))
                .andExpect(jsonPath("$.confianca").exists());

        mvc.perform(get("/api/alertas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("S2")));
    }

    @Test
    void relatoNaoDeveRetornarContato() throws Exception {
        String json = """
                {"tipo":"FALTA_SERVICO","valor":4,"latitude":-23.5,"longitude":-46.6,"bairro":"Mooca",
                 "descricao":"sem luz, me liga 11 98888-7777","contato":"joao@email.com"}
                """;
        mvc.perform(post("/api/relatos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origem").value("COMUNIDADE"))
                .andExpect(content().string(not(containsString("joao"))))
                .andExpect(content().string(not(containsString("98888"))));
    }

    @Test
    void relatoComIntensidadeErradaDeveDar400() throws Exception {
        String json = """
                {"tipo":"CALOR","valor":9,"latitude":-23.5,"longitude":-46.6,"bairro":"Se"}
                """;
        mvc.perform(post("/api/relatos").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resumoDeveResponder() throws Exception {
        mvc.perform(get("/api/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists());
    }
}
