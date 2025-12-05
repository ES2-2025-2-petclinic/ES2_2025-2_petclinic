package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.samples.petclinic.owner.Visit;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.ui.Model;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WelcomeControllerTests {

	@Mock
	private VetRepository vetRepository;

	@Mock
	private OwnerRepository ownerRepository;

	@Mock
	private Model model;

	private WelcomeController welcomeController;

	@BeforeEach
	void setUp() {
		welcomeController = new WelcomeController(vetRepository, ownerRepository);
	}

	@Test
	void shouldAddAttributesToModel() {
		given(vetRepository.findAll()).willReturn(List.of(new Vet(), new Vet()));
		given(ownerRepository.count()).willReturn(5L);

		Owner owner = new Owner();
		Pet pet = new Pet();
		Visit futureVisit = new Visit();
		futureVisit.setDate(LocalDate.now().plusDays(5));
		futureVisit.setDescription("Checkup");
		pet.addVisit(futureVisit);
		owner.addPet(pet);

		given(ownerRepository.findAll()).willReturn(List.of(owner));

		String viewName = welcomeController.welcome(model);

		assertThat(viewName).isEqualTo("welcome");

		verify(model).addAttribute("totalVets", 2L);
		verify(model).addAttribute("totalOwners", 5L);
		verify(model).addAttribute("totalPets", 1L);
		verify(model).addAttribute(eq("upcomingVisits"), any(List.class));
	}

	@Test
	void shouldHandleEmptyData() {
		given(vetRepository.findAll()).willReturn(Collections.emptyList());
		given(ownerRepository.count()).willReturn(0L);
		given(ownerRepository.findAll()).willReturn(Collections.emptyList());

		welcomeController.welcome(model);

		verify(model).addAttribute("totalVets", 0L);
		verify(model).addAttribute("totalOwners", 0L);
		verify(model).addAttribute("totalPets", 0L);
		verify(model).addAttribute(eq("upcomingVisits"), eq(Collections.emptyList()));
	}

	@Test
	void shouldHandleNullOwners() {
		given(vetRepository.findAll()).willReturn(Collections.emptyList());
		given(ownerRepository.count()).willReturn(0L);

		given(ownerRepository.findAll()).willReturn(null);

		String viewName = welcomeController.welcome(model);

		assertThat(viewName).isEqualTo("welcome");

		verify(model).addAttribute("totalVets", 0L);
		verify(model).addAttribute("totalOwners", 0L);

		verify(model).addAttribute("totalPets", 0L);
		verify(model).addAttribute(eq("upcomingVisits"), eq(Collections.emptyList()));
	}

}