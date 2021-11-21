package pt.iul.ista.ads.owl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

public class Ontology {
	
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	private OWLDataFactory factory;
	private IRI documentIRI;
	private PrefixManager prefixManager;
	
	private static final Charset charset = StandardCharsets.UTF_8;
	
	public Ontology(String owl) throws OntologyException {
		try {
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument(new ByteArrayInputStream(owl.getBytes(charset)));
			factory = manager.getOWLDataFactory();
			documentIRI = ontology.getOntologyID().getOntologyIRI().get();
			prefixManager = new DefaultPrefixManager(documentIRI.toString());
		} catch(OWLOntologyCreationException e) {
			throw new OntologyException(e);
		}
	}
	
	@Override
	public String toString() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			manager.saveOntology(ontology, baos);
			return baos.toString(charset);
		} catch(OWLOntologyStorageException e) {
			e.printStackTrace();
			return "Error in OWL generation";
		}
	}

	public void addClass(String className, String superclassName) {
		OWLClass newClass = factory.getOWLClass(":#" + className, prefixManager);
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(newClass));
		if(superclassName != null) {
			OWLClass superclass = factory.getOWLClass(":#" + superclassName, prefixManager);
			manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(newClass, superclass));
		}
	}
}
