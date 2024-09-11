package ufg.structures;

import com.google.gson.annotations.JsonAdapter;

import ufg.enums.PropertyType;
import ufg.gson.PropertySerializer;

@JsonAdapter(PropertySerializer.class)
public class Property {
    public PropertyType type;
    public Object value;
}
