package chpg.graph;

public class Edge extends GraphElement {

	private Node from;
	private Node to;
	
	public Edge(Node from, Node to) {
		super();
		this.from = from;
		this.to = to;
	}
	
	public Edge(String name, Node from, Node to) {
		super(name);
		this.from = from;
		this.to = to;
	}

	public Node from() {
		return from;
	}

	public Node to() {
		return to;
	}

	@Override
	public String toString() {
		return "Edge [\n\tName: " + getName() + "\n\tFrom " + from + "\n\tTo " + to + "\n\tAttributes: " + this.attributes().toString() + ", Tags: " + this.tags().toString() + "\n]";
	}
	
}
