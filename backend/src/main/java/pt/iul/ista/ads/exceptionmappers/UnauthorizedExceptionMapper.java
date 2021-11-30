package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.authorization.UnauthorizedException;
import pt.iul.ista.ads.models.ErrorResponseModel;

@Provider
public class UnauthorizedExceptionMapper implements ExceptionMapper<UnauthorizedException>{

	@Override
	public Response toResponse(UnauthorizedException exception) {
		return Response.status(401).entity(new ErrorResponseModel("unauthorized")).build();
	}

}
