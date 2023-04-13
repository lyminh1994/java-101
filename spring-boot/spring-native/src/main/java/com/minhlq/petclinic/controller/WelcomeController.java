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

import com.minhlq.petclinic.exception.BaseException;
import com.minhlq.petclinic.model.Owner;
import com.minhlq.petclinic.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
class WelcomeController {

  private final OwnerRepository ownerRepository;

  @GetMapping("/")
  public String welcome(Pageable pageable, Model model) {
    log.info("{}", pageable);
    // find owners
    Page<Owner> owners = ownerRepository.findAll(pageable);
    model.addAttribute("currentPage", pageable.getPageNumber());
    model.addAttribute("totalPages", owners.getTotalPages());
    model.addAttribute("owners", owners.getContent());

    return "welcome";
  }

  @GetMapping("/oups")
  public String triggerException() {
    throw new BaseException(
        "Expected: controller used to showcase what happens when an exception is thrown");
  }
}
