package org.springframework.samples.petclinic;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.samples.petclinic.model.BaseEntity;
import org.springframework.samples.petclinic.model.Person;
import org.springframework.samples.petclinic.vet.Vet;

import static org.assertj.core.api.Assertions.assertThat;

class PetClinicRuntimeHintsTests {

	@Test
	void registerHints() {
		RuntimeHints hints = new RuntimeHints();
		new PetClinicRuntimeHints().registerHints(hints, getClass().getClassLoader());

		assertThat(RuntimeHintsPredicates.resource().forResource("db/*")).accepts(hints);
		assertThat(RuntimeHintsPredicates.resource().forResource("messages/*")).accepts(hints);
		assertThat(RuntimeHintsPredicates.resource().forResource("mysql-default-conf")).accepts(hints);

		assertThat(RuntimeHintsPredicates.serialization().onType(BaseEntity.class)).accepts(hints);
		assertThat(RuntimeHintsPredicates.serialization().onType(Person.class)).accepts(hints);
		assertThat(RuntimeHintsPredicates.serialization().onType(Vet.class)).accepts(hints);
	}

}