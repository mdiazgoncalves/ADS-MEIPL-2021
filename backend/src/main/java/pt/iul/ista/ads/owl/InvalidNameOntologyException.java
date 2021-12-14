package pt.iul.ista.ads.owl;

public class InvalidNameOntologyException extends OntologyException {

	private static final long serialVersionUID = 1L;
	
	public InvalidNameOntologyException(String name) {
		super("Name '" + name + "' is not valid");
	}

}
