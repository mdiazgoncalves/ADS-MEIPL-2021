package pt.iul.ista.ads.services;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;

import pt.iul.ista.ads.models.*;

@Server(url = "https://knowledge-base-ads-test2.herokuapp.com")
@Path("/")
public class Services {
	
	
	// **************************************************
	// Autorização
	// **************************************************
	
	@Path("/login/editor")
	@POST
	@Operation(tags = {"Autorização"},
		summary = "Autoriza editor",
		description = "Dado um email, é devolvido um token que autoriza o editor a trabalhar numa nova versão da ontologia. "
				+ "Caso já exista um branch para esse email, não será devolvido token. "
				+ "Será necessário que esse branch seja apagado primeiro, ao ser rejeitado ou aceitado por um curador.",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = String.class))),
				@ApiResponse(responseCode = "400",
				description = "Email não informado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Email já registado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response loginEditor(@Parameter(description = "Email do editor", required = true) @QueryParam("email") String email) {
		return Response.status(501).build();
	}
	
	@Path("/login/curator")
	@POST
	@Operation(tags = {"Autorização"},
		summary = "Autoriza curador",
		description = "Dado uma password, é devolvido um token que autoriza o curador a trabalhar sobre qualquer versão da ontologia.",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = String.class))),
				@ApiResponse(responseCode = "400",
				description = "Password não informada",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Password inválida",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response loginCurator(@Parameter(description = "Password do curador") @QueryParam("password") String password) {
		return Response.status(501).build();
	}
	
	
	// **************************************************
	// Operações CRUD
	// **************************************************
	
	// Classes
	
	@Path("/classes")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Lista as classes",
		description = "Retorna a árvore de classes existentes na base de conhecimento",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClassesResponseModel.class)))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response listClasses(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token) {
		ClassesResponseModel bebida = new ClassesResponseModel();
		bebida.setName("Bebida");
		ClassesResponseModel vinho = new ClassesResponseModel();
		vinho.setName("Vinho");
		bebida.setSubclasses(Stream.of(vinho).collect(Collectors.toList()));
		return Response.ok(Stream.of(bebida).collect(Collectors.toList())).build();
		
//		LatestCommitResponseModel res = new LatestCommitResponseModel();
//		res.setBranch("ze_manel@gmail.com");
//		res.setLatestCommit("2343abcdef");
//		return Response.status(409).entity(res).build();
	}

	
	@Path("/class/{class}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de classe",
		description = "Retorna detalhes sobre uma classe, nomeadamente os indivíduos que pertencem à classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = ClassDetailResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailClass(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token,
			@Parameter(description = "Nome de classe") @PathParam("class") String className) {
		return Response.status(501).build();
	}
	
	@Path("/class/{class}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar classe",
		description = "Cria nova classe, dado um nome e (opcionalmente) uma superclasse",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou nome de classe já existe na ontologia ou superclasse não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response createClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de classe") @PathParam("class") String className,
			@Parameter(description = "Detalhes da nova classe", required = true) ClassCreateRequestModel body) {
		return Response.status(501).build();
	}

	@Path("/class/{class}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar classe",
		description = "Altera o nome da classe e/ou a sua superclasse",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou nome de classe já existe na ontologia ou superclasse não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response alterClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de classe") @PathParam("class") String className,
			@Parameter(description = "Alterações a fazer à classe", required = true) ClassAlterRequestModel body) {
		return Response.status(501).build();
	}
	
	@Path("/class/{class}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar classe",
		description = "Apaga a classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de classe") @PathParam("class") String className) {
		return Response.status(501).build();
	}

	// Relações
	
	@Path("/relationships")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Lista as relações",
		description = "Retorna uma lista das relações existentes",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response listRelationships(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token) {
		return Response.status(501).build();
	}
	
	@Path("/relationships/{relationship}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de relação",
		description = "Retorna uma lista de pares de indivíduos que se relacionam através de uma dada relação",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = RelationshipDetailResponseModel.class)))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String individualName) {
		return Response.status(501).build();
	}
	
	
	@Path("/relationship/{relationship}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar relação",
		description = "Cria nova relação, dado um par de classes que definem os tipos de indivíduos que podem participar na relação",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou nome de relação já existe na ontologia ou classes indicadas não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response createRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName,
			@Parameter(description = "Detalhes da nova relação", required = true) RelationshipCreateRequestModel body) {
		return Response.status(501).build();
	}

	@Path("/relationship/{relationship}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar relação",
		description = "Altera o nome da relação e/ou as classes que relaciona",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou novo nome da relação já existe na ontologia ou classes não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response alterRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName,
			@Parameter(description = "Alterações a fazer à relação", required = true) RelationshipAlterRequestModel body) {
		return Response.status(501).build();
	}
	
	@Path("/relationship/{relationship}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar relação",
		description = "Apaga a classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName) {
		return Response.status(501).build();
	}
	
	// Indivíduos
	
	@Path("/individuals")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Lista os indivíduos",
		description = "Retorna uma lista dos indivíduos existentes",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response listIndividuals(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token) {
		return Response.status(501).build();
	}
	
	@Path("/individual/{individual}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de indivíduo",
		description = "Retorna a classe a que um indivíduo pertence, bem como uma lista de relações nas quais o indivíduo participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = IndividualDetailResponseModel.class)))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação") @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente") @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização") @QueryParam("token") String token,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName) {
		return Response.status(501).build();
	}
	
	@Path("/individual/{individual}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar indivíduo",
		description = "Cria nova novo indivíudo, dado a sua classe e lista de relações nas quais participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou nome de relação já existe na ontologia ou classes indicadas não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response createIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName,
			@Parameter(description = "Detalhes do novo indivíduo", required = true) IndividualCreateRequestModel body) {
		return Response.status(501).build();
	}


	@Path("/individual/{individual}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar indivíduo",
		description = "Alterar nome e/ou classe do indivíduo e/ou acrescentar/remover relações nas quais o indivíduo participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit ou novo nome da relação já existe na ontologia ou classes não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response alterIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName,
			@Parameter(description = "Alterações a fazer ao indivíduo", required = true) IndividualAlterRequestModel body) {
		return Response.status(501).build();
	}
	
	@Path("/individual/{individual}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar indivíduo",
		description = "Apaga o indivíduo",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch foi informado mas falta o commit",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName) {
		return Response.status(501).build();
	}
	
	
}
