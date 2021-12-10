package pt.iul.ista.ads.services;

import java.io.IOException;

import javax.ws.rs.Consumes;
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
import pt.iul.ista.ads.authorization.Authorization;
import pt.iul.ista.ads.authorization.UnauthorizedException;
import pt.iul.ista.ads.github.BranchAlreadyExistsException;
import pt.iul.ista.ads.github.BranchNotFoundException;
import pt.iul.ista.ads.github.GithubOperations;
import pt.iul.ista.ads.github.GithubOperations.ReadOntologyResponse;
import pt.iul.ista.ads.github.InvalidBranchException;
import pt.iul.ista.ads.github.OldCommitException;
import pt.iul.ista.ads.models.*;
import pt.iul.ista.ads.owl.Ontology;
import pt.iul.ista.ads.owl.OntologyException;
import pt.iul.ista.ads.utils.Utils;

//@Server(url = "https://knowledge-base-ads-test2.herokuapp.com")
@Server(url = "http://localhost:8080")
@Path("/")
public class Services {
	
	
	// **************************************************
	// Autorização
	// **************************************************
	
	@Path("/login/curator")
	@POST
	@Operation(tags = {"Autorização"},
		summary = "Autoriza curador",
		description = "Dado uma password, é devolvido um token que autoriza o curador a trabalhar sobre qualquer versão da ontologia.",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = String.class))),
				@ApiResponse(responseCode = "401",
				description = "Password inválida",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response loginCurator(@Parameter(description = "Password do curador", required = true) @QueryParam("password") String password) throws UnauthorizedException {
		return Response.ok(Authorization.generateToken(password)).build();
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
				content = @Content(schema = @Schema(implementation = ClassesResponseModel.class)))})
	@Produces("application/json")
	public Response listClasses(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		ClassesResponseModel res = new ClassesResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.listClasses());
		return Response.ok(res).build();
	}

	
	@Path("/class/{class}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de classe",
		description = "Retorna detalhes sobre uma classe, nomeadamente os indivíduos que pertencem à classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = ClassDetailResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Nome de classe") @PathParam("class") String className) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		ClassDetailResponseModel res = new ClassDetailResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.detailClass(className));
		return Response.ok(res).build();
	}
	
	@Path("/class/{class}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar classe",
		description = "Cria nova classe, dado um nome e (opcionalmente) uma superclasse",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou nome de classe já existe na ontologia ou superclasse não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Superclasse não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response createClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de classe") @PathParam("class") String className,
			@Parameter(description = "Detalhes da nova classe", required = true) ClassCreateRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.addClass(className, body.getSuperClassName());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}

	@Path("/class/{class}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar classe",
		description = "Altera o nome da classe e/ou a sua superclasse."
				+ " Uma superclasse null indica que a superclasse se mantém a mesma."
				+ " Uma string vazia indica que a classe deixa de ter superclasse (i.e. está no top level).",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou nome de classe já existe na ontologia ou superclasse não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response alterClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de classe") @PathParam("class") String className,
			@Parameter(description = "Alterações a fazer à classe", required = true) ClassAlterRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.alterClass(className, body.getNewClassName(), body.getNewSuperClass());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}
	
	@Path("/class/{class}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar classe",
		description = "Apaga a classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Classe não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteClass(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de classe") @PathParam("class") String className) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.deleteClass(className);
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}

	// Relações
	
	@Path("/relationships")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Lista as relações",
		description = "Retorna uma lista das relações existentes",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = RelationshipsResponseModel.class)))})
	@Produces("application/json")
	public Response listRelationships(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		RelationshipsResponseModel res = new RelationshipsResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.listRelationships());
		return Response.ok(res).build();
	}
	
	@Path("/relationship/{relationship}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de relação",
		description = "Retorna uma lista de pares de indivíduos que se relacionam através de uma dada relação",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = RelationshipDetailResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		RelationshipDetailResponseModel res = new RelationshipDetailResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.detailRelationship(relationshipName));
		return Response.ok(res).build();
	}
	
	
	@Path("/relationship/{relationship}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar relação",
		description = "Cria nova relação, dado um par de classes que definem os tipos de indivíduos que podem participar na relação",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou nome de relação já existe na ontologia ou classes indicadas não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response createRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName,
			@Parameter(description = "Detalhes da nova relação", required = true) RelationshipCreateRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.createRelationship(relationshipName, body.getClassName1(), body.getClassName2());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}

	@Path("/relationship/{relationship}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar relação",
		description = "Altera o nome da relação e/ou as classes que relaciona",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou novo nome da relação já existe na ontologia ou classes não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response alterRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName,
			@Parameter(description = "Alterações a fazer à relação", required = true) RelationshipAlterRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.alterRelationship(relationshipName, body.getNewRelationshipName(), body.getClassName1(), body.getClassName2());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}
	
	@Path("/relationship/{relationship}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar relação",
		description = "Apaga a classe",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Relação não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteRelationship(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de relação") @PathParam("relationship") String relationshipName) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.deleteRelationship(relationshipName);
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}
	
	// Indivíduos
	
	@Path("/individuals")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Lista os indivíduos",
		description = "Retorna uma lista dos indivíduos existentes",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = IndividualsResponseModel.class)))})
	@Produces("application/json")
	public Response listIndividuals(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		IndividualsResponseModel res = new IndividualsResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.listIndividuals());
		return Response.ok(res).build();
	}
	
	@Path("/individual/{individual}")
	@GET
	@Operation(tags = {"Operações CRUD"},
		summary = "Detalhes de indivíduo",
		description = "Retorna a classe a que um indivíduo pertence, bem como uma lista de relações nas quais o indivíduo participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
						content = @Content(schema = @Schema(implementation = IndividualDetailResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response detailIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		IndividualDetailResponseModel res = new IndividualDetailResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.detailIndividual(individualName));
		return Response.ok(res).build();
	}
	
	@Path("/individual/{individual}")
	@POST
	@Operation(tags = {"Operações CRUD"},
		summary = "Criar indivíduo",
		description = "Cria nova novo indivíudo, dado a sua classe e lista de relações nas quais participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou nome de relação já existe na ontologia ou classes indicadas não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response createIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName,
			@Parameter(description = "Detalhes do novo indivíduo", required = true) IndividualCreateRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.createIndividual(individualName, body.getClassName(), body.getRelationships());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}


	@Path("/individual/{individual}")
	@PUT
	@Operation(tags = {"Operações CRUD"},
		summary = "Alterar indivíduo",
		description = "Alterar nome e/ou classe do indivíduo e/ou acrescentar/remover relações nas quais o indivíduo participa",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado ou novo nome da relação já existe na ontologia ou classes não existem",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	@Consumes("application/json")
	public Response alterIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName,
			@Parameter(description = "Alterações a fazer ao indivíduo", required = true) IndividualAlterRequestModel body) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.alterIndividual(individualName, body.getNewIndividualName(), body.getClassName(), body.getRelationships());
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}
	
	@Path("/individual/{individual}")
	@DELETE
	@Operation(tags = {"Operações CRUD"},
		summary = "Apagar indivíduo",
		description = "Apaga o indivíduo",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "400",
				description = "Branch e/ou commit não informado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Indivíduo não existe",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteIndividual(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Nome de indivíduo") @PathParam("individual") String individualName) throws OldCommitException, IOException, OntologyException, InvalidBranchException, BranchNotFoundException {
		String newCommit = GithubOperations.editOntology(branch, commit, (ontology) -> {
			ontology.deleteIndividual(individualName);
		});
		return Response.ok(new LatestCommitResponseModel(newCommit, branch)).build();
	}
	
	
	// **************************************************
	// Query
	// **************************************************
	
	@Path("/query")
	@POST
	@Operation(tags = {"Query"},
		summary = "Executar query",
		description = "Executa query e retorna lista de indivíduos",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))})
	@Produces("application/json")
	@Consumes("text/plain")
	public Response query(@Parameter(description = "Nome do branch sobre o qual incide a operação", required = true) @QueryParam("branch") String branch,
			@Parameter(description = "Query", required = true) String query) throws IOException, OntologyException, BranchNotFoundException {
		ReadOntologyResponse readOntologyResponse = GithubOperations.readOntology(branch); 
		Ontology ontology = readOntologyResponse.getOntology();
		QueryResponseModel res = new QueryResponseModel();
		res.setBranch(branch);
		res.setLatestCommit(readOntologyResponse.getLatestCommit());
		res.setData(ontology.query(query));
		return Response.ok(res).build();
	}
	
	
	// **************************************************
	// Versionamento
	// **************************************************

	@Path("/branches")
	@GET
	@Operation(tags = {"Versionamento"},
		summary = "Lista os branches",
		description = "Retorna uma lista dos branches existentes",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response listBranches(@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token) throws IOException, UnauthorizedException {
		Authorization.checkValidToken(token);
		return Response.ok(GithubOperations.listBranches()).build();
	}
	
	@Path("/branch/{branch}/merge")
	@POST
	@Operation(tags = {"Versionamento"},
		summary = "Aceita versão",
		description = "Aceita uma versão da ontologia, ao fazer merge do branch passado por parâmetro com o master. Caso haja conflitos será necessário usar um outro serviço",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Merge não efetuado devido a conflito ou o commit indicado no request não é o mais recente",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Branch inexistente",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response mergeBranch(@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch) throws IOException, BranchNotFoundException, OldCommitException, UnauthorizedException {
		Authorization.checkValidToken(token);
		GithubOperations.mergeBranch(branch, commit);
		return Response.ok().build();
	}
	
	@Path("/branch/{branch}")
	@DELETE
	@Operation(tags = {"Versionamento"},
		summary = "Apaga versão",
		description = "Rejeita uma versão da ontologia ao apagar o branch respetivo",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "Parâmetro \"commit\" não se refere ao commit mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Branch inexistente",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response deleteBranch(@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch) throws IOException, BranchNotFoundException, OldCommitException, UnauthorizedException {
		Authorization.checkValidToken(token);
		GithubOperations.deleteBranch(branch, commit);
		return Response.ok().build();
	}
	
	@Path("/branch/{branch}/owl")
	@GET
	@Operation(tags = {"Versionamento"},
	summary = "Obtém OWL de branch",
	description = "Devolve o OWL do branch, de maneira a que o curador possa alterar o OWL manualmente de forma a fazer merge",
	responses = {@ApiResponse(responseCode = "200",
			description = "OK"),
			@ApiResponse(responseCode = "401",
			description = "Não autorizado",
			content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
			@ApiResponse(responseCode = "404",
			description = "Branch inexistente",
			content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/xml")
	public Response getBranchOwl(@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch) throws IOException, UnauthorizedException {
		Authorization.checkValidToken(token);
		return Response.ok(GithubOperations.getBranchOwl(branch)).build();
	}
	
	@Path("/branch/{branch}/mergeowl")
	@POST
	@Operation(tags = {"Versionamento"},
		summary = "Aceita versão, dado um OWL",
		description = "Aceita uma versão da ontologia. O OWL passado pelo body do request é considerado como sendo o OWL do dado branch",
		responses = {@ApiResponse(responseCode = "200",
				description = "OK"),
				@ApiResponse(responseCode = "409",
				description = "O commit indicado no request não é o mais recente",
				content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
				@ApiResponse(responseCode = "401",
				description = "Não autorizado",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
				@ApiResponse(responseCode = "404",
				description = "Branch inexistente",
				content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Consumes("application/xml")
	public Response mergeBranchOwl(@Parameter(description = "Hash do commit mais recente conhecido pelo cliente", required = true) @QueryParam("commit") String commit,
			@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch,
			@Parameter(description = "OWL a substituir a versão existente") String body) throws IOException, BranchNotFoundException, OldCommitException, UnauthorizedException {
		Authorization.checkValidToken(token);
		GithubOperations.mergeBranchOwl(branch, commit, body);
		return Response.ok().build();
	}
	
	@Path("/branch/{branch}/latest")
	@GET
	@Operation(tags = {"Versionamento"},
	summary = "Commit mais recente do branch",
	description = "Retorna o commit mais recente existente num branch",
	responses = {@ApiResponse(responseCode = "200",
			description = "OK",
			content = @Content(schema = @Schema(implementation = LatestCommitResponseModel.class))),
			@ApiResponse(responseCode = "401",
			description = "Não autorizado",
			content = @Content(schema = @Schema(implementation = ErrorResponseModel.class))),
			@ApiResponse(responseCode = "404",
			description = "Branch inexistente",
			content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response branchLatestCommit(@Parameter(description = "Token de autorização", required = true) @QueryParam("token") String token,
			@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch) throws IOException, BranchNotFoundException {
		String commit = GithubOperations.getLatestCommit(branch);
		LatestCommitResponseModel res = new LatestCommitResponseModel(commit, branch);
		return Response.ok(res).build();
	}
	
	@Path("/branch/{branch}")
	@POST
	@Operation(tags = {"Versionamento"},
	summary = "Cria novo branch",
	description = "Cria novo branch a partir do OWL que se encontra no branch principal. Esta operação não requer token pois pode ser despoletada por um editor.",
	responses = {@ApiResponse(responseCode = "200",
			description = "OK"),
			@ApiResponse(responseCode = "400",
			description = "Branch já existe ou branch não corresponde a um email válido",
			content = @Content(schema = @Schema(implementation = ErrorResponseModel.class)))})
	@Produces("application/json")
	public Response createBranch(@Parameter(description = "Nome do branch sobre o qual incide a operação") @PathParam("branch") String branch) throws InvalidBranchException, IOException {
		if(!Utils.validateEmail(branch)) {
			ErrorResponseModel res = new ErrorResponseModel("invalid email");
			return Response.status(400).entity(res).build();
		}

		String branchLower = branch.toLowerCase();
		try {
			GithubOperations.createBranch(branchLower);
		} catch(BranchAlreadyExistsException e) {
			return Response.status(400).entity(new ErrorResponseModel("branch already exists")).build();
		}
		return Response.ok().build();
	}
	
}
