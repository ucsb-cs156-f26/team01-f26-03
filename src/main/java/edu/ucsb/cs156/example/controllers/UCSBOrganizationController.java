package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UCSBOrganizations")
@RequestMapping("/api/ucsborganizations")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * THis method returns a list of all ucsborganizations.
   *
   * @return a list of all ucsborganizations
   */
  @Operation(summary = "List all UCSB organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
    return orgs;
  }

  /**
   * This method creates a new organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization
   * @param orgTranslationShort short form organzation translation
   * @param orgTranslation long form organization translation
   * @param inactive whether or not the organization is active
   * @return the new organization
   */
  @Operation(summary = "Create a new UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {

    UCSBOrganization org = new UCSBOrganization();
    org.setOrgCode(orgCode);
    org.setOrgTranslationShort(orgTranslationShort);
    org.setOrgTranslation(orgTranslation);
    org.setInactive(inactive);

    UCSBOrganization savedOrg = ucsbOrganizationRepository.save(org);
    return savedOrg;
  }
}
