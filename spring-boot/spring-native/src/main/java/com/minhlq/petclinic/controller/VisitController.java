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
import com.minhlq.petclinic.model.Visit;
import com.minhlq.petclinic.repository.OwnerRepository;
import jakarta.validation.Valid;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
@RequiredArgsConstructor
public class VisitController {

  private final OwnerRepository owners;

  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  /**
   * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure we always
   * have fresh data - Since we do not use the session scope, make sure that Pet object always has
   * an id (Even though id is not part of the form fields)
   *
   * @param petId
   * @return Pet
   */
  @ModelAttribute("visit")
  public Visit loadPetWithVisit(
      @PathVariable("ownerId") int ownerId,
      @PathVariable("petId") int petId,
      Map<String, Object> model) {
    Owner owner = owners.findById(ownerId);

    Pet pet = owner.getPet(petId);
    model.put("pet", pet);
    model.put("owner", owner);

    Visit visit = new Visit();
    pet.addVisit(visit);
    return visit;
  }

  // Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is
  // called
  @GetMapping("/owners/{ownerId}/pets/{petId}/visits/new")
  public String initNewVisitForm(@PathVariable String ownerId, @PathVariable String petId) {
    return "pets/createOrUpdateVisitForm";
  }

  // Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is
  // called
  @PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
  public String processNewVisitForm(
      @ModelAttribute Owner owner,
      @PathVariable int petId,
      @Valid Visit visit,
      BindingResult result,
      @PathVariable String ownerId) {
    if (result.hasErrors()) {
      return "pets/createOrUpdateVisitForm";
    }

    owner.addVisit(petId, visit);
    owners.save(owner);
    return "redirect:/owners/{ownerId}";
  }
}
