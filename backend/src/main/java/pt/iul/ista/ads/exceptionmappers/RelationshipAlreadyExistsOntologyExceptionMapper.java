package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.models.ErrorResponseModel;
import pt.iul.ista.ads.owl.RelationshipAlreadyExistsOntologyException;

@Provider
public class RelationshipAlreadyExistsOntologyExceptionMapper implements ExceptionMapper<RelationshipAlreadyExistsOntologyException> {

	@Override
	public Response toResponse(RelationshipAlreadyExistsOntologyException exception) {
		return Response.status(400).entity(new ErrorResponseModel(exception.getMessage())).build();
	}

}
