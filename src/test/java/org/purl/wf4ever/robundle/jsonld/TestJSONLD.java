package org.purl.wf4ever.robundle.jsonld;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.UUID;

import org.junit.Test;

import com.github.jsonldjava.core.JSONLD;
import com.github.jsonldjava.impl.JenaJSONLDSerializer;
import com.github.jsonldjava.utils.JSONUtils;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestJSONLD {
	private static final String RO = "http://purl.org/wf4ever/ro#";
	private static final String ORE = "http://www.openarchives.org/ore/terms/";

	
	@Test
	public void generateFakeManifest() throws Exception {
		OntModel m = ModelFactory.createOntologyModel();
		String base = "widget://" + UUID.randomUUID() + "/";
		Ontology ont = m.createOntology( base + ".ro/manifest.json" );
		m.setDynamicImports(true);
		ont.addImport( m.createResource( "http://purl.org/wf4ever/ro" ) );
		OntClass cls = m.getOntClass(RO + "ResearchObject");
		Individual ro = m.createIndividual(base, cls);
		
		ObjectProperty aggregates = m.getObjectProperty( ORE + "aggregates" );
		
		OntClass roResource = m.getOntClass(RO + "Resource");
		ro.addProperty(aggregates, m.createIndividual(base + "hello.txt", roResource));
		ro.addProperty(aggregates, m.createIndividual(base + "folder/test.txt", roResource));


		
		// Clone the model to avoid serializing all the OWL stuff
		Model m2 = ModelFactory.createDefaultModel();
		Writer writer = new StringWriter();		
		m.write(writer);
		
		System.out.println(writer.toString());
		
		Reader reader = new StringReader(writer.toString());
		m2.read(reader, base);
		m2.setNsPrefix("ro", ORE);
		m2.setNsPrefix("ore", ORE);
		m2.setNsPrefix("bundle", base);
        JenaJSONLDSerializer serializer = new JenaJSONLDSerializer();
        
        Object output = JSONLD.fromRDF(m2, serializer);

        boolean expand = false;
		if (expand) {
            output = JSONLD.expand(output);
        }
        boolean frame = true;
		if (frame) {
			Object inframe = JSONUtils.fromInputStream(getClass().getResourceAsStream("frame.json"));
			inframe  = JSONLD.expand(inframe);
			output = JSONLD.frame(output, inframe);
        }
        boolean simplify = false;
		if (simplify) {
        	output = JSONLD.simplify(output);
        }

        if (output != null) {
            System.out.println(JSONUtils.toString(output));
        }

		
	}
}
