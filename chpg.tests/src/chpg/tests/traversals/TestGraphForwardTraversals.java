package chpg.tests.traversals;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import chpg.graph.Edge;
import chpg.graph.Graph;
import chpg.graph.Node;
import chpg.graph.PropertyGraph;
import chpg.graph.schema.SchemaGraph;

public class TestGraphForwardTraversals {

	protected PropertyGraph graph;
	
	protected Node a;
	protected Node b;
	protected Node c;
	protected Node d;
	protected Node e;
	protected Node f;
	protected Node g;
	
	protected Edge e1;
	protected Edge e2;
	protected Edge e3;
	protected Edge e4;
	protected Edge e5;
	protected Edge e6;
	
	public static final String NAME = "name";
	
	@Before
	public void setUp() throws Exception {
		resetGraph();
	}

	private void resetGraph() {
		graph = new PropertyGraph(new SchemaGraph());
		
		a = new Node();
		a.putAttr(NAME, "a");
		graph.add(a);
		
		b = new Node();
		b.putAttr(NAME, "b");
		graph.add(b);
		
		c = new Node();
		c.putAttr(NAME, "c");
		graph.add(c);
		
		d = new Node();
		d.putAttr(NAME, "d");
		graph.add(d);
		
		e = new Node();
		e.putAttr(NAME, "e");
		graph.add(e);
		
		f = new Node();
		f.putAttr(NAME, "f");
		graph.add(f);
		
		g = new Node();
		g.putAttr(NAME, "g");
		graph.add(g);
		
		e1 = new Edge(a,b);
		e1.putAttr(NAME, "e1");
		graph.add(e1);
		
		e2 = new Edge(b,c);
		e2.putAttr(NAME, "e2");
		graph.add(e2);
		
		e3 = new Edge(c,b);
		e3.putAttr(NAME, "e3");
		graph.add(e3);
		
		e4 = new Edge(c,d);
		e4.putAttr(NAME, "e4");
		graph.add(e4);
		
		e5 = new Edge(d,e);
		e5.putAttr(NAME, "e5");
		graph.add(e5);
		
		e6 = new Edge(d,g);
		e6.putAttr(NAME, "e6");
		graph.add(e6);
	}

	@Test
	public void testForwardATraversal() {
		Graph result = graph.forward(a);
		inspectForwardA(result);
	}

	protected void inspectForwardA(Graph result) {
		if(result.nodes().size() != 6){
			fail("Forward from a should include 6 nodes");
		}
		
		if(!result.nodes().contains(a)) {
			fail("Forward from a should include a");
		}
		
		if(!result.nodes().contains(b)) {
			fail("Forward from a should include b");
		}
		
		if(!result.nodes().contains(c)) {
			fail("Forward from a should include c");
		}
		
		if(!result.nodes().contains(d)) {
			fail("Forward from a should include d");
		}
		
		if(!result.nodes().contains(e)) {
			fail("Forward from a should include e");
		}
		
		if(!result.nodes().contains(g)) {
			fail("Forward from a should include g");
		}
		
		if(result.nodes().contains(f)) {
			fail("Forward from a should NOT include f");
		}
		
		if(result.edges().size() != 6){
			fail("Forward from a should include 6 edges");
		}
		
		if(!result.edges().contains(e1)) {
			fail("Forward from a should include e1");
		}
		
		if(!result.edges().contains(e2)) {
			fail("Forward from a should include e2");
		}
		
		if(!result.edges().contains(e3)) {
			fail("Forward from a should include e3");
		}
		
		if(!result.edges().contains(e4)) {
			fail("Forward from a should include e4");
		}
		
		if(!result.edges().contains(e5)) {
			fail("Forward from a should include e5");
		}
		
		if(!result.edges().contains(e6)) {
			fail("Forward from a should include e6");
		}
	}
	
	@Test
	public void testForwardBTraversal() {
		Graph result = graph.forward(b);
		inspectForwardB(result);
	}

	protected void inspectForwardB(Graph result) {
		if(result.nodes().size() != 5){
			fail("Forward from b should include 5 nodes");
		}
		
		if(result.nodes().contains(a)) {
			fail("Forward from b should NOT include a");
		}
		
		if(!result.nodes().contains(b)) {
			fail("Forward from b should include b");
		}
		
		if(!result.nodes().contains(c)) {
			fail("Forward from b should include c");
		}
		
		if(!result.nodes().contains(d)) {
			fail("Forward from b should include d");
		}
		
		if(!result.nodes().contains(e)) {
			fail("Forward from b should include e");
		}
		
		if(!result.nodes().contains(g)) {
			fail("Forward from b should include g");
		}
		
		if(result.nodes().contains(f)) {
			fail("Forward from b should NOT include f");
		}
		
		if(result.edges().size() != 5){
			fail("Forward from b should include 5 edges");
		}
		
		if(result.edges().contains(e1)) {
			fail("Forward from b should NOT include e1");
		}
		
		if(!result.edges().contains(e2)) {
			fail("Forward from b should include e2");
		}
		
		if(!result.edges().contains(e3)) {
			fail("Forward from b should include e3");
		}
		
		if(!result.edges().contains(e4)) {
			fail("Forward from b should include e4");
		}
		
		if(!result.edges().contains(e5)) {
			fail("Forward from b should include e5");
		}
		
		if(!result.edges().contains(e6)) {
			fail("Forward from b should include e6");
		}
	}
	
	@Test
	public void testForwardCTraversal() {
		Graph result = graph.forward(c);
		inspectForwardC(result);
	}

	protected void inspectForwardC(Graph result) {
		if(result.nodes().size() != 5){
			fail("Forward from c should include 5 nodes");
		}
		
		if(result.nodes().contains(a)) {
			fail("Forward from c should NOT include a");
		}
		
		if(!result.nodes().contains(b)) {
			fail("Forward from c should include b");
		}
		
		if(!result.nodes().contains(c)) {
			fail("Forward from c should include c");
		}
		
		if(!result.nodes().contains(d)) {
			fail("Forward from c should include d");
		}
		
		if(!result.nodes().contains(e)) {
			fail("Forward from c should include e");
		}
		
		if(!result.nodes().contains(g)) {
			fail("Forward from c should include g");
		}
		
		if(result.nodes().contains(f)) {
			fail("Forward from c should NOT include f");
		}
		
		if(result.edges().size() != 5){
			fail("Forward from c should include 5 edges");
		}
		
		if(result.edges().contains(e1)) {
			fail("Forward from c should NOT include e1");
		}
		
		if(!result.edges().contains(e2)) {
			fail("Forward from c should include e2");
		}
		
		if(!result.edges().contains(e3)) {
			fail("Forward from c should include e3");
		}
		
		if(!result.edges().contains(e4)) {
			fail("Forward from c should include e4");
		}
		
		if(!result.edges().contains(e5)) {
			fail("Forward from c should include e5");
		}
		
		if(!result.edges().contains(e6)) {
			fail("Forward from c should include e6");
		}
	}
	
	@Test
	public void testForwardEGTraversal() {
		Graph result = graph.forward(e,g);
		inspectForwardEG(result);
	}

	protected void inspectForwardEG(Graph result) {
		if(result.nodes().size() != 2){
			fail("Forward from eg should include 2 nodes");
		}
		
		if(!result.nodes().contains(e)) {
			fail("Forward from eg should include e");
		}
		
		if(!result.nodes().contains(g)) {
			fail("Forward from eg should include g");
		}
		
		if(!result.edges().isEmpty()){
			fail("Forward from eg should include 0 edges");
		}
	}
	
	@Test
	public void testForwardFTraversal() {
		Graph result = graph.forward(f);
		inspectForwardF(result);
	}

	protected void inspectForwardF(Graph result) {
		if(result.nodes().size() != 1){
			fail("Forward from f should include 1 nodes");
		}
		
		if(!result.nodes().contains(f)) {
			fail("Forward from f should include f");
		}

		if(!result.edges().isEmpty()){
			fail("Forward from f should include 0 edges");
		}
	}

}
