package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.models.ErrorResponseModel;
import pt.iul.ista.ads.owl.ClassNotFoundOntologyException;

@Provider
public class ClassNotFoundOntologyExceptionMapper implements ExceptionMapper<ClassNotFoundOntologyException> {

	@Override
	public Response toResponse(ClassNotFoundOntologyException exception) {
		return Response.status(404).entity(new ErrorResponseModel(exception.getMessage())).build();
	}

}
