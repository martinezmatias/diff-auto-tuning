package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.Test;

import fr.gumtree.autotuning.domain.ParameterDomain;
import fr.gumtree.autotuning.outils.LoadSpecification;

public class LoadSpecificationTest {

	@Test
	public void testSpect() throws Exception {
		File spec1 = new File("./examples/specification/spec1.xml");

		LoadSpecification ls = new LoadSpecification();
		List<ParameterDomain> dd = ls.retrieveParameters(spec1);
		assertEquals(5, dd.size());

		ParameterDomain p1 = dd.stream().filter(e -> e.getId().equals("parD")).findFirst().get();
		assertNotNull(p1);

		assertEquals(5, p1.computeInterval().length);

		assertEquals(0.1, p1.computeInterval()[0]);
		assertEquals(0.3, p1.computeInterval()[1]);
		assertEquals(0.5, p1.computeInterval()[2]);

		assertEquals(0.3, p1.getDefaultValue());

		// Values
		ParameterDomain p2 = dd.stream().filter(e -> e.getId().equals("parD2")).findFirst().get();
		assertNotNull(p2);

		assertEquals(3, p2.computeInterval().length);

		assertEquals(0.25, p2.computeInterval()[0]);
		assertEquals(0.5, p2.computeInterval()[1]);
		assertEquals(0.75, p2.computeInterval()[2]);

		assertEquals(0.5, p2.getDefaultValue());

		//
		ParameterDomain pi1 = dd.stream().filter(e -> e.getId().equals("parI")).findFirst().get();
		assertNotNull(pi1);

		assertEquals(10, pi1.computeInterval().length);

		assertEquals(1, pi1.computeInterval()[0]);
		assertEquals(2, pi1.computeInterval()[1]);
		assertEquals(3, pi1.computeInterval()[2]);

		assertEquals(5, pi1.getDefaultValue());

		//
		ParameterDomain piv = dd.stream().filter(e -> e.getId().equals("parIv")).findFirst().get();
		assertNotNull(piv);

		assertEquals(3, piv.computeInterval().length);

		assertEquals(2, piv.computeInterval()[0]);
		assertEquals(5, piv.computeInterval()[1]);
		assertEquals(8, piv.computeInterval()[2]);

		assertEquals(8, piv.getDefaultValue());

		//
		ParameterDomain ps = dd.stream().filter(e -> e.getId().equals("parS1")).findFirst().get();
		assertNotNull(ps);

		assertEquals(3, ps.computeInterval().length);

		assertEquals("size", ps.computeInterval()[0]);
		assertEquals("length", ps.computeInterval()[1]);
		assertEquals("weight", ps.computeInterval()[2]);

		assertEquals("length", ps.getDefaultValue());

	}

}
