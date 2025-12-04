/*
 * Copyright 2012-2025 the original author or authors.
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

package org.springframework.samples.petclinic.system;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomeController {

	private final VetRepository vetRepository;

	private final OwnerRepository ownerRepository;

	public WelcomeController(VetRepository vetRepository, OwnerRepository ownerRepository) {
		this.vetRepository = vetRepository;
		this.ownerRepository = ownerRepository;
	}

	@GetMapping("/")
	public String welcome(Model model) {
		// Total de veterinários
		long totalVets = vetRepository.findAll().size();

		// Total de proprietários
		long totalOwners = ownerRepository.count();

		// Total de pets ativos
		List<Owner> owners = ownerRepository.findAll();
		long totalPets = owners.stream().mapToLong(owner -> owner.getPets().size()).sum();

		// Próximas 5 visitas agendadas
		LocalDate today = LocalDate.now();
		List<Visit> upcomingVisits = owners.stream()
			.flatMap(owner -> owner.getPets().stream())
			.flatMap(pet -> pet.getVisits().stream())
			.filter(visit -> !visit.getDate().isBefore(today))
			.sorted((v1, v2) -> v1.getDate().compareTo(v2.getDate()))
			.limit(5)
			.collect(Collectors.toList());

		model.addAttribute("totalVets", totalVets);
		model.addAttribute("totalOwners", totalOwners);
		model.addAttribute("totalPets", totalPets);
		model.addAttribute("upcomingVisits", upcomingVisits);

		return "welcome";
	}

}
