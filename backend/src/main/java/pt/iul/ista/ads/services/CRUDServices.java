package pt.iul.ista.ads.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;

import pt.iul.ista.ads.models.ClassesModel;

@Path("/")
@Tag(name="Operações CRUD")
public class CRUDServices {
	
	@Path("/class")
	@GET
	@Operation(summary = "Lista as classes",
		description = "Retorna a árvore de classes existentes na base de conhecimento",
		responses = {@ApiResponse(responseCode = "200",
				description = "Árvore de classes",
				content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClassesModel.class))))})
	@Produces("application/json")
	public List<ClassesModel> listClasses() {
		ClassesModel bebida = new ClassesModel();
		bebida.setName("Bebida");
		ClassesModel vinho = new ClassesModel();
		vinho.setName("Vinho");
		bebida.setSubclasses(Stream.of(vinho).collect(Collectors.toList()));
		return Stream.of(bebida).collect(Collectors.toList());
	}


}
