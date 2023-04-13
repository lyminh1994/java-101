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
package com.minhlq.petclinic.controller;

import com.minhlq.petclinic.model.Owner;
import com.minhlq.petclinic.model.Pet;
import com.minhlq.petclinic.model.PetType;
import com.minhlq.petclinic.model.PetValidator;
import com.minhlq.petclinic.repository.OwnerRepository;
import jakarta.validation.Valid;
import java.util.Collection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
@RequestMapping("/owners/{ownerId}")
@RequiredArgsConstructor
public class PetController {

  private static final String VIEWS_PETS_CREATE_OR_UPDATE_FORM = "pets/createOrUpdatePetForm";

  private final OwnerRepository owners;

  @ModelAttribute("types")
  public Collection<PetType> populatePetTypes() {
    return owners.findPetTypes();
  }

  @ModelAttribute("owner")
  public Owner findOwner(@PathVariable int ownerId) {
    return owners.findById(ownerId);
  }

  @ModelAttribute("pet")
  public Pet findPet(
      @PathVariable int ownerId,
      @PathVariable(required = false) Integer petId) {
    return petId == null ? new Pet() : owners.findById(ownerId).getPet(petId);
  }

  @InitBinder("owner")
  public void initOwnerBinder(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @InitBinder("pet")
  public void initPetBinder(WebDataBinder dataBinder) {
    dataBinder.setValidator(new PetValidator());
  }

  @GetMapping("/pets/new")
  public String initCreationForm(Owner owner, ModelMap model, @PathVariable String ownerId) {
    Pet pet = new Pet();
    owner.addPet(pet);
    model.put("pet", pet);
    return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
  }

  @PostMapping("/pets/new")
  public String processCreationForm(
      Owner owner,
      @Valid Pet pet,
      BindingResult result,
      ModelMap model,
      @PathVariable String ownerId) {
    if (StringUtils.hasLength(pet.getName())
        && pet.isNew()
        && owner.getPet(pet.getName(), true) != null) {
      result.rejectValue("name", "duplicate", "already exists");
    }

    owner.addPet(pet);
    if (result.hasErrors()) {
      model.put("pet", pet);
      return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    owners.save(owner);
    return "redirect:/owners/{ownerId}";
  }

  @GetMapping("/pets/{petId}/edit")
  public String initUpdateForm(
      Owner owner, @PathVariable int petId, ModelMap model, @PathVariable String ownerId) {
    model.put("pet", owner.getPet(petId));
    return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
  }

  @PostMapping("/pets/{petId}/edit")
  public String processUpdateForm(
      @Valid Pet pet,
      Owner owner,
      BindingResult result,
      ModelMap model,
      @PathVariable String ownerId,
      @PathVariable String petId) {
    if (result.hasErrors()) {
      model.put("pet", pet);
      return VIEWS_PETS_CREATE_OR_UPDATE_FORM;
    }

    owner.addPet(pet);
    owners.save(owner);
    return "redirect:/owners/{ownerId}";
  }
}
