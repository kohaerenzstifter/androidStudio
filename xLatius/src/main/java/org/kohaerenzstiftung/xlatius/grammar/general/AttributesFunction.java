package org.kohaerenzstiftung.xlatius.grammar.general;

import java.util.Iterator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class AttributesFunction {

	static public AttributesFunction instantiate(JSONObject source) throws Throwable {
		AttributesFunction result = null;

		@SuppressWarnings("unchecked")
		Iterator<String> keys = source.keys();
		String key = null;
		while (keys.hasNext()) {
			if (key != null) {
				throw new Throwable("more than one keys!");
			}
			key = keys.next();
		}
		if (key == null) {
			throw new Throwable("key missing!");
		}

		boolean and;
		if (key.equals("value")) {
			String value = source.getString("value");
			result = new ValueAttributesFunction(value);
		} else if (key.equals("not")) {
			JSONObject jsonNegated = source.getJSONObject("not");
			AttributesFunction negated = instantiate(jsonNegated);
			result = new NotAttributesFunction(negated);
		} else if ((and = key.equals("and"))||key.equals("or")) {
			JSONArray jsonArray = source.getJSONArray(key);
			LinkedList<AttributesFunction> array =
					new LinkedList<AttributesFunction>();
			int length = jsonArray.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonMember = jsonArray.getJSONObject(i);
				array.add(instantiate(jsonMember));
			}
			result = and ? new AndAttributesFunction(array) :
				new OrAttributesFunction(array);
		} else {
			throw new Throwable("unknown attributes function " + key);
		}

		return result;
	}

	public boolean isValue() {
		return false;
	}

	public boolean isNot() {
		return false;
	}

	public boolean isAnd() {
		return false;
	}

	public boolean isOr() {
		return false;
	}
}
