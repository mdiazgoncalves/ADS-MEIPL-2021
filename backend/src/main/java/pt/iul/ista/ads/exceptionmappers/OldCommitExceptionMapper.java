package pt.iul.ista.ads.exceptionmappers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import pt.iul.ista.ads.github.OldCommitException;
import pt.iul.ista.ads.models.LatestCommitResponseModel;

@Provider
public class OldCommitExceptionMapper implements ExceptionMapper<OldCommitException> {

	@Override
	public Response toResponse(OldCommitException exception) {
		return Response.status(409).entity(new LatestCommitResponseModel(exception.getLatestCommit(), exception.getBranch())).build();
	}

}
