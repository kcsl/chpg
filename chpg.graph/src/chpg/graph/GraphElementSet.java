package chpg.graph;

import java.util.Collection;
import java.util.Objects;

public abstract class GraphElementSet<E extends GraphElement> extends GraphElementCollection<E> {

	public GraphElementSet() {}

	public GraphElementSet(Iterable<E> iterable) {
		addAll(iterable);
	}

	public GraphElementSet(E element) {
		add(element);
	}

	public GraphElementSet(E[] elements) {
		for(E element : elements) {
			add(element);
		}
	}
	
	/**
	 * Returns this collection as a standard set type
	 * @return
	 */
	public abstract Collection<E> toStandardSet();
	
	/**
	 * Returns this collection as a standard collection type
	 * @return
	 */
	public Collection<E> toStandardCollection(){
		return toStandardSet();
	}
	
	/**
	 * Returns the set of elements from this graph that are tagged with all of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public GraphElementSet<E> taggedWithAny(String... tags){
		GraphElementSet<E> result = new GraphElementHashSet<E>();
		for(E e : this){
			for(String tag : tags){
				if(e.tags().contains(tag)){
					result.add(e);
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the set of elements from this graph that are tagged with any of the
	 * given tags
	 * 
	 * @param tags
	 * @return
	 */
	public GraphElementSet<E> taggedWithAll(String... tags){
		GraphElementSet<E> result = new GraphElementHashSet<E>();
		for(E e : this){
			boolean add = true;
			for(String tag : tags){
				if(!e.tags().contains(tag)){
					add = false;
					break;
				}
			}
			if(add){
				result.add(e);
			}
		}
		return result;
	}
	
	/**
	 * Returns a graph element set filtered to elements with the attribute key and value
	 * @param attribute
	 * @param value
	 * @return
	 */
	public GraphElementSet<E> filter(String attribute){
		GraphElementSet<E> result = new GraphElementHashSet<E>();
		for(E e : this){
			if(e.hasAttr(attribute)){
				result.add(e);
			}
		}
		return result;
	}
	
	/**
	 * Returns a graph element set filtered to elements with the attribute key and value
	 * @param attribute
	 * @param value
	 * @return
	 */
	public GraphElementSet<E> filter(String attribute, Object... values){
		GraphElementSet<E> result = new GraphElementHashSet<E>();
		if(attribute != null && values != null){
			for(E e : this){
				if(e.hasAttr(attribute)) {
					Object attributeValue = e.attributes().get(attribute);
					if(attributeValue != null) {
						for(Object value : values) {
							if(value != null) {
								if(Objects.equals(attributeValue, value)) {
									result.add(e);
									break;
								}
							}
						}
					}
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "GraphElementSet [size=" + size() + "]";
	}
	
}
