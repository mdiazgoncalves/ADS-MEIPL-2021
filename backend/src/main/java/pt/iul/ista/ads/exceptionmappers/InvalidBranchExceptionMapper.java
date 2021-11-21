package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.github.InvalidBranchException;
import pt.iul.ista.ads.models.ErrorResponseModel;

@Provider
public class InvalidBranchExceptionMapper implements ExceptionMapper<InvalidBranchException> {

	@Override
	public Response toResponse(InvalidBranchException exception) {
		return Response.status(400).entity(new ErrorResponseModel("invalid branch")).build();
	}

}
