package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.models.ErrorResponseModel;
import pt.iul.ista.ads.owl.RelationshipNotFoundOntologyException;

@Provider
public class RelationshipNotFoundOntologyExceptionMapper implements ExceptionMapper<RelationshipNotFoundOntologyException> {

	@Override
	public Response toResponse(RelationshipNotFoundOntologyException exception) {
		return Response.status(400).entity(new ErrorResponseModel(exception.getMessage())).build();
	}

}
