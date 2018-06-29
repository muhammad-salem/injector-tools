package org.injector.tools.proxy;

import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


public class SocketList {
	
	private HashMap<Integer, Socket> list = new HashMap<>();

	public int size() {
		return list.size();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public boolean containsKey(Integer key) {
		return list.containsKey(key);
	}

	public boolean containsValue(Socket value) {
		return list.containsValue(value);
	}

	public Socket get(Integer key) {
		return list.get(key);
	}

	public Socket put(Integer key, Socket value) {
		return list.put(key, value);
	}

	public Socket remove(Integer key) {
		return list.remove(key);
	}

	public void putAll(Map<? extends Integer, ? extends Socket> m) {
		list.putAll(m);
	}

	public void clear() {
		list.clear();
	}

	public Set<Integer> keySet() {
		return list.keySet();
	}

	public Collection<Socket> values() {
		return list.values();
	}

	public Set<Entry<Integer, Socket>> entrySet() {
		return list.entrySet();
	}

	public boolean equalsPort(Object o) {
		return list.equals(o);
	}

	public Socket getOrDefault(Integer key, Socket defaultValue) {
		return list.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super Integer, ? super Socket> action) {
		list.forEach(action);
	}

	public void replaceAll(
			BiFunction<? super Integer, ? super Socket, ? extends Socket> function) {
		list.replaceAll(function);
	}

	public Socket putIfAbsent(Integer key, Socket value) {
		return list.putIfAbsent(key, value);
	}

	public boolean remove(Integer key, Socket value) {
		return list.remove(key, value);
	}

	public boolean replace(Integer key, Socket oldValue, Socket newValue) {
		return list.replace(key, oldValue, newValue);
	}

	public Socket replace(Integer key, Socket value) {
		return list.replace(key, value);
	}

	public Socket computeIfAbsent(Integer key,
			Function<? super Integer, ? extends Socket> mappingFunction) {
		return list.computeIfAbsent(key, mappingFunction);
	}

	public Socket computeIfPresent(Integer key,
			BiFunction<? super Integer, ? super Socket, ? extends Socket> remappingFunction) {
		return list.computeIfPresent(key, remappingFunction);
	}

	public Socket compute(Integer key,
			BiFunction<? super Integer, ? super Socket, ? extends Socket> remappingFunction) {
		return list.compute(key, remappingFunction);
	}

	public Socket merge(Integer key, Socket value,
			BiFunction<? super Socket, ? super Socket, ? extends Socket> remappingFunction) {
		return list.merge(key, value, remappingFunction);
	}
	
}
