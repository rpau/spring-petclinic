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

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/owners")
class OwnerRestController {

	private final OwnerRepository ownerRepository;

	public OwnerRestController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	@GetMapping
	public List<OwnerDto> listOwners() {
		Pageable pageable = PageRequest.of(0, 20);
		return this.ownerRepository.findAll(pageable).stream().map(OwnerDto::from).toList();
	}

	@GetMapping("/{ownerId}")
	public ResponseEntity<OwnerDto> getOwner(@PathVariable Integer ownerId) {
		Owner owner = this.ownerRepository.findById(ownerId);
		if (owner != null) {
			return ResponseEntity.ok(OwnerDto.from(owner));
		}
		return ResponseEntity.notFound().build();
	}

}
