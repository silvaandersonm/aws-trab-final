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

import br.com.andersillva.trabfinal.api.dto.TransferenciaAlteracaoDTO;
import br.com.andersillva.trabfinal.api.dto.TransferenciaDTO;
import br.com.andersillva.trabfinal.api.dto.TransferenciaInclusaoDTO;
import br.com.andersillva.trabfinal.api.exception.RespostaPadraoErro;
import br.com.andersillva.trabfinal.api.util.ConstantesSwagger;
import br.com.andersillva.trabfinal.api.util.DTOFactory;
import br.com.andersillva.trabfinal.api.util.EntityFactory;
import br.com.andersillva.trabfinal.api.util.VersaoAPI;
import br.com.andersillva.trabfinal.domain.model.Atleta;
import br.com.andersillva.trabfinal.domain.model.Clube;
import br.com.andersillva.trabfinal.domain.model.Transferencia;
import br.com.andersillva.trabfinal.domain.service.AtletaService;
import br.com.andersillva.trabfinal.domain.service.ClubeService;
import br.com.andersillva.trabfinal.domain.service.TransferenciaService;
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
public class TransferenciaResource {

	@Autowired
	private TransferenciaService transferenciaService;

	@Autowired
	private ClubeService clubeService;

	@Autowired
	private AtletaService atletaService;

	@Operation(summary = "Obtém a lista de transferências.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Requisição processada com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.NO_CONTENT, description="Não existe nenhuma transferência cadastrada.")})
	@GetMapping("/transferencias")
	public ResponseEntity<List<TransferenciaDTO>> listarTransferencias() {
		List<Transferencia> transferencias = transferenciaService.listar();
		if (transferencias.size() > 0) {
			List<TransferenciaDTO> transferenciasDTO = DTOFactory.getTransferenciaDTO(transferencias);
			return new ResponseEntity<>(transferenciasDTO, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
	}

	@Operation(summary = "Obtém uma transferência a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Requisição processada com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.NOT_FOUND, description="Transferência não encontrada.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@GetMapping("/transferencias/{id}")
	public ResponseEntity<TransferenciaDTO> obterTransferencia(@PathVariable("id") Long id) {
		Transferencia transferencia = transferenciaService.obterPorId(id);
		TransferenciaDTO transferenciaDTO = DTOFactory.getTransferenciaDTO(transferencia);
		return new ResponseEntity<>(transferenciaDTO, HttpStatus.OK);
	}

	@Operation(summary = "Cria uma nova transferência.")
	@ApiResponses(value = {@ApiResponse(responseCode="201", description="Transferência criada com sucesso. A URL do novo recurso é adicionada cabeçalho Location."),
						   @ApiResponse(responseCode=ConstantesSwagger.BAD_REQUEST, description=ConstantesSwagger.BAD_REQUEST_DESCRIPTION, content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))}),
						   @ApiResponse(responseCode=ConstantesSwagger.CONFLICT, description="Transferência informada já existe.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@PostMapping(path="/transferencias", consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> incluirTransferencia(@Valid @RequestBody TransferenciaInclusaoDTO transferenciaInclusaoDTO) {
		Transferencia transferencia = mapearTransferencia(transferenciaInclusaoDTO);
		transferenciaService.incluir(transferencia);
		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(transferencia.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}

	@Operation(summary = "Altera uma transferência existente a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Transferência alterada com sucesso."),
			   			   @ApiResponse(responseCode=ConstantesSwagger.BAD_REQUEST, description=ConstantesSwagger.BAD_REQUEST_DESCRIPTION, content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))}),
			   			   @ApiResponse(responseCode=ConstantesSwagger.NOT_FOUND, description="Transferência não encontrada.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))}),
			   			   @ApiResponse(responseCode=ConstantesSwagger.CONFLICT, description="Já existe outra tranferência igual registrada.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@PutMapping(path="/transferencias/{id}", consumes=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> alterarTransferencia(@PathVariable("id") Long id, 
													 @Valid @RequestBody TransferenciaAlteracaoDTO transferenciaAlteracaoDTO) {
		Transferencia transferencia = mapearTransferencia(transferenciaAlteracaoDTO, id);
		transferenciaService.alterar(transferencia);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@Operation(summary = "Exclui uma transferência existente a partir de seu ID.")
	@ApiResponses(value = {@ApiResponse(responseCode=ConstantesSwagger.OK, description="Transferência excluída com sucesso."),
						   @ApiResponse(responseCode=ConstantesSwagger.CONFLICT, description="Transferência não pode ser excluída, pois possui dependências.", content={@Content(mediaType=MediaType.APPLICATION_JSON_VALUE, schema=@Schema(implementation = RespostaPadraoErro.class))})})
	@DeleteMapping("/transferencias/{id}")
	public ResponseEntity<Void> excluirTransferencia(@PathVariable("id") Long id) {
		transferenciaService.excluir(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	private Transferencia mapearTransferencia(TransferenciaInclusaoDTO transferenciaInclusaoDTO) {
		Clube clubeOrigem = clubeService.obterPorId(transferenciaInclusaoDTO.getIdClubeOrigem());
		Clube clubeDestino = clubeService.obterPorId(transferenciaInclusaoDTO.getIdClubeDestino());
		Atleta atleta = atletaService.obterPorId(transferenciaInclusaoDTO.getIdAtleta());

		Transferencia transferencia = EntityFactory.getTransferencia(transferenciaInclusaoDTO);
		transferencia.setClubeOrigem(clubeOrigem);
		transferencia.setClubeDestino(clubeDestino);
		transferencia.setAtleta(atleta);

		return transferencia;
	}

	private Transferencia mapearTransferencia(TransferenciaAlteracaoDTO transferenciaAlteracaoDTO, Long id) {
		Transferencia transferencia = transferenciaService.obterPorId(id);
		transferencia.setData(transferenciaAlteracaoDTO.getData());
		transferencia.setValor(transferenciaAlteracaoDTO.getValor());
		transferencia.setMoeda(transferenciaAlteracaoDTO.getMoeda());
		return transferencia;
	}

}
