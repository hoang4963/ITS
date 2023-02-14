package com.its.econtract.controllers.request.type;

import java.util.Map;

public interface PersistentEnum<T> {

	T getValue();

	String getDisplayName();

	Map<T, ? extends PersistentEnum<T>> getAll();
}
