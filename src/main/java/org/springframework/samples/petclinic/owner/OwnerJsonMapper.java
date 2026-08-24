package org.springframework.samples.petclinic.owner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;

final class OwnerJsonMapper {

	private static final JsonMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

	private OwnerJsonMapper() {
	}

	static String toPrettyJson(OwnerDto owner) throws JsonProcessingException {
		return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(owner);
	}

	static OwnerDto fromJson(String json) throws JsonProcessingException {
		return MAPPER.readValue(json, OwnerDto.class);
	}

}
