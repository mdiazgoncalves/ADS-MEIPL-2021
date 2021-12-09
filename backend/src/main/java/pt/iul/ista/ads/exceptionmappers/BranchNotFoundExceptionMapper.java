package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.github.BranchNotFoundException;
import pt.iul.ista.ads.models.ErrorResponseModel;

@Provider
public class BranchNotFoundExceptionMapper implements ExceptionMapper<BranchNotFoundException> {

	@Override
	public Response toResponse(BranchNotFoundException exception) {
		return Response.status(404).entity(new ErrorResponseModel(exception.getMessage())).build();
	}

}
