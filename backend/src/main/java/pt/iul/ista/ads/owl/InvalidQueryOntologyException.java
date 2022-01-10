package pt.iul.ista.ads.owl;

public class InvalidQueryOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;

	public InvalidQueryOntologyException(String query) {
		super("Query '" + query + "' is not valid");
	}
}
