package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.Funcionario;
import com.vitaclinica.agendamento.dto.request.CadastrarFuncionarioRequest;
import com.vitaclinica.agendamento.dto.response.FuncionarioResponse;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.mapper.FuncionarioMapper;
import com.vitaclinica.agendamento.repository.FuncionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {

    private final FuncionarioRepository repo;
    private final FuncionarioMapper     mapper;
    private final PasswordEncoder       passwordEncoder;

    @Transactional
    public FuncionarioResponse cadastrar(CadastrarFuncionarioRequest req) {
        String cpf = normalizarCpf(req.getCpf());
        if (repo.existsByCpf(cpf))
            throw new NegocioException("CPF_DUPLICADO", "Já existe um funcionário com este CPF.", HttpStatus.CONFLICT);

        Funcionario f = Funcionario.builder()
                .nome(req.getNome())
                .cpf(cpf)
                .email(req.getEmail())
                .telefone(req.getTelefone())
                .senhaHash(passwordEncoder.encode(req.getSenha()))
                .role(req.getRole())
                .build();
        return mapper.toResponse(repo.save(f));
    }

    public List<FuncionarioResponse> listar() {
        return repo.findByAtivoTrueOrderByNomeAsc().stream().map(mapper::toResponse).toList();
    }

    public FuncionarioResponse buscarPorId(Long id) {
        return mapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public void desativar(Long id) {
        Funcionario f = buscarEntidade(id);
        f.setAtivo(false);
        repo.save(f);
    }

    private Funcionario buscarEntidade(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionário", id));
    }

    private String normalizarCpf(String cpf) {
        String n = cpf.replaceAll("[^0-9]", "");
        return n.substring(0,3)+"."+n.substring(3,6)+"."+n.substring(6,9)+"-"+n.substring(9);
    }
}
