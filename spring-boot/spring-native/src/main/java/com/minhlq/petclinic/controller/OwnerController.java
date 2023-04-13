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
import com.minhlq.petclinic.repository.OwnerRepository;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 */
@Controller
@RequestMapping("/owners")
@RequiredArgsConstructor
public class OwnerController {

  private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

  private final OwnerRepository owners;

  @InitBinder
  public void setAllowedFields(WebDataBinder dataBinder) {
    dataBinder.setDisallowedFields("id");
  }

  @ModelAttribute("owner")
  public Owner findOwner(@PathVariable(required = false) Integer ownerId) {
    return ownerId == null ? new Owner() : owners.findById(ownerId);
  }

  @GetMapping("/new")
  public String initCreationForm(ModelMap model) {
    model.put("owner", new Owner());
    return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
  }

  @PostMapping("/new")
  public String processCreationForm(@Valid Owner owner, BindingResult result) {
    if (result.hasErrors()) {
      return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    owners.save(owner);
    return "redirect:/owners/" + owner.getId();
  }

  @GetMapping("/find")
  public String initFindForm() {
    return "owners/findOwners";
  }

  @GetMapping
  public String processFindForm(
      @RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result, Model model) {
    // allow parameterless GET request for /owners to return all records
    if (owner.getLastName() == null) {
      owner.setLastName(""); // empty string signifies broadest possible search
    }

    // find owners by last name
    Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, owner.getLastName());
    if (ownersResults.isEmpty()) {
      // no owners found
      result.rejectValue("lastName", "notFound", "not found");
      return "owners/findOwners";
    }

    if (ownersResults.getTotalElements() == 1) {
      // 1 owner found
      owner = ownersResults.iterator().next();
      return "redirect:/owners/" + owner.getId();
    }

    // multiple owners found
    return addPaginationModel(page, model, ownersResults);
  }

  private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
    model.addAttribute("listOwners", paginated);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", paginated.getTotalPages());
    model.addAttribute("totalItems", paginated.getTotalElements());
    model.addAttribute("owners", paginated.getContent());
    return "owners/ownersList";
  }

  private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
    int pageSize = 5;
    Pageable pageable = PageRequest.of(page - 1, pageSize);
    return owners.findByLastName(lastname, pageable);
  }

  @GetMapping("/{ownerId}/edit")
  public String initUpdateOwnerForm(@PathVariable int ownerId, Model model) {
    Owner owner = owners.findById(ownerId);
    model.addAttribute(owner);
    return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
  }

  @PostMapping("/{ownerId}/edit")
  public String processUpdateOwnerForm(
      @Valid Owner owner, BindingResult result, @PathVariable int ownerId) {
    if (result.hasErrors()) {
      return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
    }

    owner.setId(ownerId);
    owners.save(owner);
    return "redirect:/owners/{ownerId}";
  }

  /**
   * Custom handler for displaying an owner.
   *
   * @param ownerId the ID of the owner to display
   * @return a ModelMap with the model attributes for the view
   */
  @GetMapping("/{ownerId}")
  public ModelAndView showOwner(@PathVariable int ownerId) {
    ModelAndView mav = new ModelAndView("owners/ownerDetails");
    mav.addObject(owners.findById(ownerId));
    return mav;
  }
}
