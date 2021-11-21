package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import io.fusionauth.jwt.InvalidJWTException;
import pt.iul.ista.ads.models.ErrorResponseModel;

@Provider
public class InvalidJWTExceptionMapper implements ExceptionMapper<InvalidJWTException> {

	@Override
	public Response toResponse(InvalidJWTException exception) {
		return Response.status(401).entity(new ErrorResponseModel("invalid token")).build();
	}

}
