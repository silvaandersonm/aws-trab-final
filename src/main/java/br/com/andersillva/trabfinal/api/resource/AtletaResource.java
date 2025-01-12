package br.com.andersillva.trabfinal.api.resource;

import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.andersillva.trabfinal.api.dto.AtletaDTO;
import br.com.andersillva.trabfinal.api.dto.AtletaInclusaoDTO;
import br.com.andersillva.trabfinal.api.dto.AtletaPersistenciaDTO;
import br.com.andersillva.trabfinal.api.dto.AtletaResumoDTO;
import br.com.andersillva.trabfinal.api.exception.RespostaPadraoErro;
import br.com.andersillva.trabfinal.api.util.ConstantesSwagger;
import br.com.andersillva.trabfinal.api.util.DTOFactory;
import br.com.andersillva.trabfinal.api.util.EntityFactory;
import br.com.andersillva.trabfinal.api.util.VersaoAPI;
import br.com.andersillva.trabfinal.domain.model.Atleta;
import br.com.andersillva.trabfinal.domain.model.Clube;
import br.com.andersillva.trabfinal.domain.service.AtletaService;
import br.com.andersillva.trabfinal.domain.service.ClubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping(path=VersaoAPI.URI_BASE_V1, produces=MediaType.APPLICATION_JSON_VALUE)
@ApiResponses(value={@ApiResponse(responseCode=ConstantesSwagger.UNAUTHORIZED, description=ConstantesSwagger.UNAUTHORIZED_DESCRIPTION, content={@Content(schema=@Schema(hidden=true))}),
					 @ApiResponse(responseCode=ConstantesSwagger.FORBIDDEN, description=ConstantesSwagger.FORBIDDEN_DESCRIPTION, content={@Content(schema=@Schema(hidden=true))}),
					 @ApiResponse(responseCode=ConstantesSwagger.INTERNAL_SERVER_ERROR, description=ConstantesSwagger.INTERNAL_SERVER_ERROR_DESCRIPTION, content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
public class AtletaResource {

	@Autowired
	private AtletaService atletaService;

	@Autowired
	private ClubeService clubeService;

	@Operation(summary = "Obtém a lista de atletas.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Requisição processada com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.NO_CONTENT, description="Não existe nenhum atleta cadastrado.")})
	@GetMapping("/atletas")
	public ResponseEntity<List<AtletaResumoDTO>> listarAtletas() {
		List<Atleta> atletas = atletaService.listar();
		if (atletas.size() > 0) {
			List<AtletaResumoDTO> atletasResumoDTO = DTOFactory.getAtletaResumoDTO(atletas);
			return new ResponseEntity<>(atletasResumoDTO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Operation(summary = "Obtém um atleta a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Requisição processada com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.NOT_FOUND, description="Atleta não encontrado.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@GetMapping("/atletas/{id}")
	public ResponseEntity<AtletaDTO> obterAtleta(@PathVariable("id") Long id) {
		Atleta atleta = atletaService.obterPorId(id);
		AtletaDTO atletaDTO = DTOFactory.getAtletaDTO(atleta);
		return new ResponseEntity<>(atletaDTO, HttpStatus.OK);
	}

	@Operation(summary = "Cria um novo atleta.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.CREATED, description="Atleta criado com sucesso. A URL do novo recurso é adicionada cabeçalho Location."),
						   @ApiResponse(responseCode=ConstantesSwagger.BAD_REQUEST, description=ConstantesSwagger.BAD_REQUEST_DESCRIPTION, content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))}),
						   @ApiResponse(responseCode=ConstantesSwagger.NOT_FOUND, description="Clube não encontrado.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@PostMapping(path="/atletas", consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> incluirAtleta(@Valid @RequestBody AtletaInclusaoDTO atletaInclusaoDTO) {
		Atleta atleta = EntityFactory.getAtleta(atletaInclusaoDTO);
		Clube clube = clubeService.obterPorId(atletaInclusaoDTO.getIdClube());
		atleta.setClube(clube);
		atletaService.incluir(atleta);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(atleta.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@Operation(summary = "Altera um atleta existente a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Atleta alterado com sucesso."),
			   			   @ApiResponse(responseCode=ConstantesSwagger.BAD_REQUEST, description=ConstantesSwagger.BAD_REQUEST_DESCRIPTION, content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))}),
			   			   @ApiResponse(responseCode=ConstantesSwagger.NOT_FOUND, description="Atleta ou clube não encontrado.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@PutMapping(path="/atletas/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> alterarAtleta(@PathVariable("id") Long id, @Valid @RequestBody AtletaPersistenciaDTO atletaPersistenciaDTO) {
		Atleta atleta = EntityFactory.getAtleta(atletaPersistenciaDTO);
		Clube clube = atletaService.obterPorId(id).getClube();
		atleta.setClube(clube);
		atleta.setId(id);
		atletaService.alterar(atleta);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Exclui um atleta existente a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Atleta excluído com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.CONFLICT, description="Atleta não pode ser excluído, pois possui dependências.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@DeleteMapping("/atletas/{id}")
	public ResponseEntity<Void> excluirAtleta(@PathVariable("id") Long id) {
		atletaService.excluir(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

}
