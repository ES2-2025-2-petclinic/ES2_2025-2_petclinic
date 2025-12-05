/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.owner;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(value = PetController.class,
		includeFilters = @ComponentScan.Filter(value = PetTypeFormatter.class, type = FilterType.ASSIGNABLE_TYPE))
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OwnerRepository owners;

	@MockitoBean
	private PetTypeRepository types;

	@BeforeEach
	void setup() {
		PetType cat = new PetType();
		cat.setId(3);
		cat.setName("hamster");
		given(this.types.findPetTypes()).willReturn(List.of(cat));

		Owner owner = new Owner();
		Pet pet = new Pet();
		Pet dog = new Pet();
		owner.addPet(pet);
		owner.addPet(dog);
		pet.setId(TEST_PET_ID);
		dog.setId(TEST_PET_ID + 1);
		pet.setName("petty");
		dog.setName("doggy");
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));
	}

	@Test
	void testInitCreationForm() throws Exception {
		mockMvc.perform(get("/owners/{ownerId}/pets/new", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(view().name("pets/createOrUpdatePetForm"))
			.andExpect(model().attributeExists("pet"));
	}

	@Test
	void testProcessCreationFormSuccess() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testLoadPetWithVisitOwnerNotFound() throws Exception {
		int NON_EXISTENT_OWNER_ID = 999;
		given(this.owners.findById(NON_EXISTENT_OWNER_ID)).willReturn(Optional.empty());

		// Attempts to access any pet-related URL with a non-existent owner ID should
		// result in an Exception
		// due to IllegalArgumentException thrown in the controller
		org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/owners/{ownerId}/pets/new", NON_EXISTENT_OWNER_ID));
		});
	}

	@Test
	void testLoadPetWithVisitPetNotFound() throws Exception {
		// Arrange
		// The setup provides owner with ID 1, pets with IDs 1 and 2.
		// Using a different ID (e.g., 999) will cause owner.getPet(petId) to return
		// null.
		int NON_EXISTENT_PET_ID = 999;

		// The controller returns null for "pet", which causes a template error when
		// rendering.
		// We assert that an exception occurs.
		org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () -> {
			mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, NON_EXISTENT_PET_ID));
		});
	}

	@Test
	void testProcessUpdateFormSameName() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
				.param("name", "petty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateFormWithNotExistingPetId() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12")
				.param("id", "99"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessCreationFormWithExistingId() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12")
				.param("id", "99"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessCreationFormHasErrors {

		@Test
		void testProcessCreationFormWithBlankName() throws Exception {
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
					.param("name", "\t \n")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithDuplicateName() throws Exception {
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
					.param("name", "petty")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithMissingPetType() throws Exception {
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
					.param("name", "Betty")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "type"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "type", "required"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessCreationFormWithInvalidBirthDate() throws Exception {
			LocalDate currentDate = LocalDate.now();
			String futureBirthDate = currentDate.plusMonths(1).toString();
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);

			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(emptyFile)
					.param("name", "Betty")
					.param("birthDate", futureBirthDate))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testInitUpdateForm() throws Exception {
			mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID))
				.andExpect(status().isOk())
				.andExpect(model().attributeExists("pet"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

	@Test
	void testProcessUpdateFormSuccess() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Nested
	class ProcessUpdateFormHasErrors {

		@Test
		void testProcessUpdateFormWithInvalidBirthDate() throws Exception {
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
					.param("name", " ")
					.param("birthDate", "2015/02/12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessUpdateFormWithBlankName() throws Exception {
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
					.param("name", "  ")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "required"))
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessUpdateFormWithFutureBirthDate() throws Exception {
			LocalDate futureDate = LocalDate.now().plusDays(1);
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
					.param("name", "Betty")
					.param("type", "hamster")
					.param("birthDate", futureDate.toString()))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "birthDate"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "birthDate", "typeMismatch.birthDate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

		@Test
		void testProcessUpdateFormWithDuplicateName() throws Exception {
			int ANOTHER_PET_ID = TEST_PET_ID + 1; // Pet with name "doggy"
			String EXISTING_NAME = "petty"; // Name of pet with ID 1
			MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);

			mockMvc
				.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, ANOTHER_PET_ID).file(emptyFile)
					.param("name", EXISTING_NAME)
					.param("type", "hamster")
					.param("birthDate", "2015-02-12"))
				.andExpect(model().attributeHasNoErrors("owner"))
				.andExpect(model().attributeHasErrors("pet"))
				.andExpect(model().attributeHasFieldErrors("pet", "name"))
				.andExpect(model().attributeHasFieldErrorCode("pet", "name", "duplicate"))
				.andExpect(status().isOk())
				.andExpect(view().name("pets/createOrUpdatePetForm"));
		}

	}

	@Test
	void testProcessCreationFormSuccessWithImage() throws Exception {
		MockMultipartFile file = new MockMultipartFile("imageFile", "test.png", "image/png",
				"test image content".getBytes());
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/new", TEST_OWNER_ID).file(file)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testProcessUpdateFormSuccessWithImage() throws Exception {
		MockMultipartFile file = new MockMultipartFile("imageFile", "test.png", "image/png",
				"test image content".getBytes());
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(file)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));
	}

	@Test
	void testGetPetImageSuccess() throws Exception {
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		pet.setImage("image content".getBytes());
		Owner owner = new Owner();
		owner.getPets().add(pet);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/image", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().contentType(org.springframework.http.MediaType.IMAGE_PNG))
			.andExpect(content().bytes("image content".getBytes()));
	}

	@Test
	void testGetPetImageNotFound() throws Exception {
		Owner owner = new Owner();
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/image", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isNotFound());
	}

	@Test
	void testProcessUpdateFormAddsNewPetWhenIdChanges() throws Exception {
		MockMultipartFile emptyFile = new MockMultipartFile("imageFile", new byte[0]);
		mockMvc
			.perform(multipart("/owners/{ownerId}/pets/{petId}/edit", TEST_OWNER_ID, TEST_PET_ID).file(emptyFile)
				.param("name", "Betty")
				.param("type", "hamster")
				.param("birthDate", "2015-02-12")
				.param("id", "99"))
			.andExpect(status().is3xxRedirection())
			.andExpect(view().name("redirect:/owners/{ownerId}"));

		ArgumentCaptor<Owner> ownerCaptor = ArgumentCaptor.forClass(Owner.class);
		verify(owners).save(ownerCaptor.capture());
	}

	@Test
	void testGetPetImageDefault() throws Exception {
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		PetType type = new PetType();
		type.setName("hamster");
		pet.setType(type);
		Owner owner = new Owner();
		owner.getPets().add(pet);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/image", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().contentType(org.springframework.http.MediaType.IMAGE_PNG));
	}

	@Test
	void testGetPetImageFallback() throws Exception {
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		PetType type = new PetType();
		type.setName("unknown");
		pet.setType(type);
		Owner owner = new Owner();
		owner.getPets().add(pet);
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/image", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isOk())
			.andExpect(content().contentType(org.springframework.http.MediaType.IMAGE_PNG));
	}

	@Test
	void testGetPetImagePetNotFoundInOwner() throws Exception {
		Owner owner = new Owner();
		given(this.owners.findById(TEST_OWNER_ID)).willReturn(Optional.of(owner));

		mockMvc.perform(get("/owners/{ownerId}/pets/{petId}/image", TEST_OWNER_ID, TEST_PET_ID))
			.andExpect(status().isNotFound());
	}

}