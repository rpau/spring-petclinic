/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OwnerDto(@JsonProperty("id") Integer id,

		@JsonProperty("firstName") String firstName,

		@JsonProperty("lastName") String lastName,

		@JsonProperty("address") String address,

		@JsonProperty("city") String city,

		@JsonProperty("telephone") String telephone,

		@JsonProperty("pets") List<PetDto> pets) {

	public static OwnerDto from(Owner owner) {
		return new OwnerDto(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getAddress(),
				owner.getCity(), owner.getTelephone(), owner.getPets().stream().map(PetDto::from).toList());
	}

}
