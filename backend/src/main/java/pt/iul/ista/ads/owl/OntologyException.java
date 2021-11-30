package pt.iul.ista.ads.owl;

public class OntologyException extends Exception {

	private static final long serialVersionUID = 1L;

	public OntologyException(Exception e) {
		super(e);
	}
	
	public OntologyException(String s) {
		super(s);
	}
}
