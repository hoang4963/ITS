package com.its.econtract.controllers.request.type;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public final class PersistentEnums {
	//
	private static final ConcurrentMap<Class<?>, Type> TYPE_ARGUMENTS = new ConcurrentHashMap<>(512);

	public static <T extends PersistentEnum<?>> Type getTypeArgument(Class<T> clazz) {
		//
		Type r = TYPE_ARGUMENTS.get(clazz);
		if(r != null) return r;

		//
		Type interfaces[] = clazz.getGenericInterfaces();
		if(interfaces == null || interfaces.length == 0) {
			interfaces = clazz.getEnclosingClass().getGenericInterfaces();
		}

		//
		for(int i = 0; i < interfaces.length; i++) {
			if(!(interfaces[i] instanceof ParameterizedType)) continue;
			final ParameterizedType pt = (ParameterizedType)interfaces[i];
			if(pt.getRawType() == PersistentEnum.class) {r = pt.getActualTypeArguments()[0]; break;}
		}
		if(r != null) r = putIfAbsent(TYPE_ARGUMENTS, clazz, r);
		return r;
	}

	public static <K, V> V putIfAbsent(ConcurrentMap<K, V> m, K k, V v) {
		final V r = m.putIfAbsent(k, v); return r != null ? r : v;
	}

//	public static <V, T extends Enum<T> & PersistentEnum<V>> Map<V, T> objects(Class<T> clazz) {
//		final T[] constants = SharedSecrets.getJavaLangAccess().getEnumConstantsShared(clazz);
//		final Map<V, T> r = new HashMap<>(); for(T t : constants) r.put(t.getValue(), t); return r;
//	}

}
