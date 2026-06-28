package com.vitaclinica.agendamento.service;

import com.vitaclinica.agendamento.domain.Profissional;
import com.vitaclinica.agendamento.dto.request.CadastrarProfissionalRequest;
import com.vitaclinica.agendamento.dto.response.ProfissionalResponse;
import com.vitaclinica.agendamento.exception.*;
import com.vitaclinica.agendamento.mapper.ProfissionalMapper;
import com.vitaclinica.agendamento.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository repo;
    private final EspecialidadeRepository especialidadeRepo;
    private final ProfissionalMapper mapper;

    @Transactional
    public ProfissionalResponse cadastrar(CadastrarProfissionalRequest req) {
        if (repo.existsByCrm(req.getCrm()))
            throw new NegocioException("CRM_DUPLICADO", "Já existe um profissional com este CRM.", HttpStatus.CONFLICT);
        var especialidade = especialidadeRepo.findById(req.getEspecialidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Especialidade", req.getEspecialidadeId()));
        var p = Profissional.builder()
                .nome(req.getNome()).crm(req.getCrm())
                .especialidade(especialidade).valorConsulta(req.getValorConsulta())
                .build();
        return mapper.toResponse(repo.save(p));
    }

    public List<ProfissionalResponse> listar() {
        return repo.findAllAtivosComEspecialidade().stream().map(mapper::toResponse).toList();
    }

    public List<ProfissionalResponse> listarPorEspecialidade(Long especialidadeId) {
        return repo.findByEspecialidadeIdAndAtivoTrue(especialidadeId).stream().map(mapper::toResponse).toList();
    }

    public ProfissionalResponse buscarPorId(Long id) {
        return mapper.toResponse(repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id)));
    }

    @Transactional
    public void desativar(Long id) {
        Profissional p = repo.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
        p.setAtivo(false);
        repo.save(p);
    }
}
