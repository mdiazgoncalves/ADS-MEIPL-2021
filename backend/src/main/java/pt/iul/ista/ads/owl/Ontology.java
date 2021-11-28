package pt.iul.ista.ads.owl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
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
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

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

	public void addClass(String className, String superclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException {
		checkClassNotExists(className);
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
	
	private void checkClassNotExists(String className) throws ClassAlreadyExistsOntologyException {
		if(ontology.containsClassInSignature(IRI.create(documentIRI.toString() + "#" + className)))
			throw new ClassAlreadyExistsOntologyException(className);
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
	
	public ClassDetailResponseModel.ClassModel detailClass(String className) throws ClassNotFoundOntologyException {
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
	
	public void alterClass(String className, String newClassName, String newSuperclassName) throws ClassNotFoundOntologyException, ClassAlreadyExistsOntologyException {
		// validar os parâmetros primeiro
		checkClassExists(className);
		if(newClassName != null) {
			checkClassNotExists(newClassName);
		}
		if(newSuperclassName != null) {
			checkClassExists(newSuperclassName);
		}
		
		// parâmetros validados; efetuar alterações
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		if(newClassName != null) {
			OWLEntityRenamer renamer = new OWLEntityRenamer(manager, Collections.singleton(ontology));
			OWLClass newCls = factory.getOWLClass(":#" + newClassName, prefixManager);
			manager.applyChanges(renamer.changeIRI(cls.getIRI(), newCls.getIRI()));
			cls = newCls; // alterar cls para que código abaixo se refira ao novo nome da classe
		}
		if(newSuperclassName != null) {
			for(OWLSubClassOfAxiom axiom : ontology.getSubClassAxiomsForSubClass(cls))
				ontology.removeAxiom(axiom);
			
			OWLClass superClass = factory.getOWLClass(":#" + newSuperclassName, prefixManager);
			manager.addAxiom(ontology, factory.getOWLSubClassOfAxiom(cls, superClass));
		}

	}
	
	public void deleteClass(String className) throws ClassNotFoundOntologyException {
		checkClassExists(className);
		OWLEntityRemover remover = new OWLEntityRemover(Collections.singleton(ontology));
		OWLClass cls = factory.getOWLClass(":#" + className, prefixManager);
		cls.accept(remover);
		manager.applyChanges(remover.getChanges());
	}
}
