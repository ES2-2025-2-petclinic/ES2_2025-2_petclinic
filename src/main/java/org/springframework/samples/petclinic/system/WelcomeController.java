package org.springframework.samples.petclinic.system;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class WelcomeController {

	private final VetRepository vetRepository;

	private final OwnerRepository ownerRepository;

	WelcomeController(VetRepository vetRepository, OwnerRepository ownerRepository) {
		this.vetRepository = vetRepository;
		this.ownerRepository = ownerRepository;
	}

	@GetMapping("/")
	public String welcome(Model model) {
		// Total de veterinários
		Collection<Vet> vets = this.vetRepository.findAll();
		long totalVets = (vets != null) ? vets.size() : 0;

		// Total de proprietários
		long totalOwners = this.ownerRepository.count();

		// Total de pets ativos e Visitas
		List<Owner> owners = this.ownerRepository.findAll();
		long totalPets = 0;
		List<Visit> upcomingVisits = Collections.emptyList();

		if (owners != null) {
			// Conta pets verificando se a lista de pets não é nula
			totalPets = owners.stream().map(Owner::getPets).filter(Objects::nonNull).mapToLong(Collection::size).sum();

			// Filtra visitas futuras com verificação de nulo em cascata
			LocalDate today = LocalDate.now();
			upcomingVisits = owners.stream()
				.map(Owner::getPets)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.map(Pet::getVisits)
				.filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.filter(visit -> visit.getDate() != null && !visit.getDate().isBefore(today))
				.sorted(Comparator.comparing(Visit::getDate))
				.limit(5)
				.collect(Collectors.toList());
		}

		model.addAttribute("totalVets", totalVets);
		model.addAttribute("totalOwners", totalOwners);
		model.addAttribute("totalPets", totalPets);
		model.addAttribute("upcomingVisits", upcomingVisits);

		return "welcome";
	}

}