package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.Paciente;
import com.vitaclinica.agendamento.dto.request.*;
import com.vitaclinica.agendamento.dto.response.PacienteResponse;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.mapper.PacienteMapper;
import com.vitaclinica.agendamento.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository    pacienteRepo;
    private final PlanoSaudeRepository  planoRepo;
    private final PacienteMapper        mapper;
    private final PasswordEncoder       passwordEncoder;

    @Transactional
    public PacienteResponse cadastrar(CadastrarPacienteRequest req) {
        String cpf = normalizarCpf(req.getCpf());
        if (pacienteRepo.existsByCpf(cpf))
            throw new NegocioException("CPF_DUPLICADO", "Já existe um cadastro com este CPF.", HttpStatus.CONFLICT);
        if (pacienteRepo.existsByEmail(req.getEmail()))
            throw new NegocioException("EMAIL_DUPLICADO", "Já existe um cadastro com este email.", HttpStatus.CONFLICT);

        Paciente paciente = mapper.toEntity(req);
        paciente.setCpf(cpf);
        paciente.setSenhaHash(passwordEncoder.encode(req.getSenha()));
        if (req.getPlanoSaudeId() != null)
            paciente.setPlanoSaude(planoRepo.findById(req.getPlanoSaudeId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Plano de saúde", req.getPlanoSaudeId())));

        return mapper.toResponse(pacienteRepo.save(paciente));
    }

    public Page<PacienteResponse> listar(String termo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Paciente> resultado = (termo == null || termo.isBlank())
                ? pacienteRepo.findAllAtivos(pageable)
                : pacienteRepo.buscarPorNomeOuCpf(termo, pageable);
        return resultado.map(mapper::toResponse);
    }

    public PacienteResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public PacienteResponse atualizar(Long id, AtualizarPacienteRequest req) {
        Paciente paciente = buscarEntidade(id);
        if (req.getNomeCompleto()  != null) paciente.setNomeCompleto(req.getNomeCompleto());
        if (req.getDataNascimento()!= null) paciente.setDataNascimento(req.getDataNascimento());
        if (req.getEmail()         != null) paciente.setEmail(req.getEmail());
        if (req.getTelefone()      != null) paciente.setTelefone(req.getTelefone());
        if (req.getCep()           != null) paciente.setCep(req.getCep());
        if (req.getLogradouro()    != null) paciente.setLogradouro(req.getLogradouro());
        if (req.getNumero()        != null) paciente.setNumero(req.getNumero());
        if (req.getComplemento()   != null) paciente.setComplemento(req.getComplemento());
        if (req.getBairro()        != null) paciente.setBairro(req.getBairro());
        if (req.getCidade()        != null) paciente.setCidade(req.getCidade());
        if (req.getUf()            != null) paciente.setUf(req.getUf());
        if (req.getPlanoSaudeId()  != null)
            paciente.setPlanoSaude(planoRepo.findById(req.getPlanoSaudeId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Plano de saúde", req.getPlanoSaudeId())));
        return mapper.toResponse(pacienteRepo.save(paciente));
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Paciente paciente = buscarEntidade(id);
        paciente.setAtivo(ativo);
        pacienteRepo.save(paciente);
    }

    private Paciente buscarEntidade(Long id) {
        return pacienteRepo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    private String normalizarCpf(String cpf) {
        String n = cpf.replaceAll("[^0-9]", "");
        return n.substring(0,3)+"."+n.substring(3,6)+"."+n.substring(6,9)+"-"+n.substring(9);
    }
}
