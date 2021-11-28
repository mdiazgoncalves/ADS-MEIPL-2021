package pt.iul.ista.ads.owl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import pt.iul.ista.ads.models.ClassDetailResponseModel;
import pt.iul.ista.ads.models.ClassesResponseModel;

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

	public void addClass(String className, String superclassName) throws ClassNotFoundOntologyException {
		OWLClass newClass = factory.getOWLClass(":#" + className, prefixManager);
		manager.addAxiom(ontology, factory.getOWLDeclarationAxiom(newClass));
		if(superclassName != null) {
			checkClassExists(superclassName);
			OWLClass superclass = factory.getOWLClass(":#" + superclassName, prefixManager);
			manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(newClass, superclass));
		}
	}
	
	private void checkClassExists(String className) throws ClassNotFoundOntologyException {
		if(!ontology.containsClassInSignature(IRI.create(documentIRI.toString() + "#" + className)))
			throw new ClassNotFoundOntologyException(className);
	}
	
	public List<ClassesResponseModel.ClassModel> listClasses() {
		Set<OWLClass> classes = ontology.getClassesInSignature();
		List<ClassesResponseModel.ClassModel> res = new ArrayList<ClassesResponseModel.ClassModel>();
		for(OWLClass cls : classes) {
			Set<OWLSubClassOfAxiom> superclassAxioms = ontology.getSubClassAxiomsForSubClass(cls);
			if(superclassAxioms.isEmpty())
				res.add(classModel(cls));
		}
		return res;
	}
	
	private ClassesResponseModel.ClassModel classModel(OWLClass cls) {
		ClassesResponseModel.ClassModel res = new ClassesResponseModel.ClassModel();
		res.setClassName(owlEntityToString(cls));
		res.setSubclasses(new ArrayList<ClassesResponseModel.ClassModel>());
		for(OWLSubClassOfAxiom subclassAxiom : ontology.getSubClassAxiomsForSuperClass(cls)) {
			res.getSubclasses().add(classModel(subclassAxiom.getSubClass().asOWLClass()));
		}
		return res;
	}
	
	private String owlEntityToString(OWLEntity entity) {
		return entity.toStringID().replace(documentIRI, "").replace("#", "");
	}
	
	public ClassDetailResponseModel.ClassModel classDetails(String className) throws ClassNotFoundOntologyException {
		checkClassExists(className);
		
		ClassDetailResponseModel.ClassModel res = new ClassDetailResponseModel.ClassModel();
		
		res.setClassName(className);
		
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		Set<OWLSubClassOfAxiom> superclassAxioms = ontology.getSubClassAxiomsForSubClass(cls);
		if(!superclassAxioms.isEmpty())
			res.setSuperClass(owlEntityToString(superclassAxioms.iterator().next().getSuperClass().asOWLClass()));
		
		res.setIndividuals(new ArrayList<String>());
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
		for(OWLNamedIndividual individual : individuals) {
			OWLClassExpression classExpression = EntitySearcher.getTypes(individual, ontology).findFirst().get();
			if(owlEntityToString(classExpression.asOWLClass()).equals(className))
				res.getIndividuals().add(owlEntityToString(individual));
		}
		
		return res;
	}
}
