package com.api.spring.projections.app.estudo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.spring.projections.app.estudo.dao.FuncionarioDao;
import com.api.spring.projections.app.estudo.entity.FuncionarioEntity;

@Service
public class FuncionarioService {

	@Autowired
	private FuncionarioDao funcionarioDao;

	public List<FuncionarioEntity> buscaTodosFuncionario() {

		return funcionarioDao.buscaTodos();

	}

	public FuncionarioEntity buscaIdFuncionario(Integer id) {

		Optional<FuncionarioEntity> funcionario = funcionarioDao.buscaId(id);

		if (funcionario.isPresent()) {

			return funcionario.get();

		} else {

			return null;

		}
	}

	public Boolean removerFuncionario(Integer id) {

		if (funcionarioDao.existeFuncionario(id)) {

			funcionarioDao.remover(id);

			return true;
		}

		return false;
	}

	public FuncionarioEntity inserirFuncionario(String nome,Integer idade,double valor,String email,String endereco,String cidade,String uf,String municipio) {
		
		FuncionarioEntity funcionario = new FuncionarioEntity();
		funcionario.setNome(nome);
		funcionario.setIdade(idade);
		funcionario.setValor(valor);
		funcionario.setEmail(email);
		funcionario.setEndereco(endereco);
		funcionario.setCidade(cidade);
		funcionario.setUf(uf);
		funcionario.setMunicipio(municipio);
		
		return funcionarioDao.inserir(funcionario);
	}

	public FuncionarioEntity atualizarFuncionario(FuncionarioEntity funcionario) {

		if (funcionarioDao.existeFuncionario(funcionario.getId())) {

			return funcionarioDao.inserir(funcionario);

		} else {

			return null;
		}
	}
}
