package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.dto.response.CepResponse;
import com.vitaclinica.agendamento.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/%s/json/";
    private final RestTemplate restTemplate;

    public CepResponse buscarCep(String cep) {
        String cepLimpo = cep.replaceAll("[^0-9]", "");
        if (cepLimpo.length() != 8) {
            throw new NegocioException("CEP_INVALIDO", "CEP deve conter 8 dígitos", HttpStatus.BAD_REQUEST);
        }
        try {
            CepResponse response = restTemplate.getForObject(
                    String.format(VIA_CEP_URL, cepLimpo), CepResponse.class);
            if (response == null || response.isErro()) {
                throw new NegocioException("CEP_NAO_ENCONTRADO",
                        "CEP " + cep + " não encontrado.", HttpStatus.NOT_FOUND);
            }
            return response;
        } catch (NegocioException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao consultar ViaCEP: {}", e.getMessage());
            throw new NegocioException("CEP_ERRO", "Serviço de CEP indisponível. Digite o endereço manualmente.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
