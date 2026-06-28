package com.vitaclinica.agendamento.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CepResponse {
    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    @JsonProperty("localidade")
    private String cidade;
    private String uf;
    private boolean erro;
}
