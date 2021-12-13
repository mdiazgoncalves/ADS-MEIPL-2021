package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.github.MergeConflictException;
import pt.iul.ista.ads.models.ErrorResponseModel;

@Provider
public class MergeConflictExceptionMapper implements ExceptionMapper<MergeConflictException> {

	@Override
	public Response toResponse(MergeConflictException exception) {
		return Response.status(409).entity(new ErrorResponseModel("can't merge because of conflict")).build();
	}

}
