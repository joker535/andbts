package cn.guye.bitshares.models;

import java.io.Serializable;

import com.google.gson.JsonElement;

/**
 * Interface to be implemented by any entity for which makes sense to
 * have a JSON-formatted string and object representation.
 */
public interface JsonSerializable extends Serializable {

    String toJsonString();

    JsonElement toJsonObject();
}