package com.embea.policy.controllers;

import com.embea.policy.facade.PolicyFacade;
import com.embea.policy.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/policy")
@AllArgsConstructor
public class PolicyController {

  private final PolicyFacade policyFacade;

  @Operation(summary = "Create Policy")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Policy Created",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PolicyCreationResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input provided",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
            })
      })
  @PostMapping("/create")
  public ResponseEntity<PolicyResponse> createPolicy(
      @Valid @RequestBody PolicyCreationRequest policyCreationRequest) {
    return ResponseEntity.ok(policyFacade.createPolicy(policyCreationRequest));
  }

  @Operation(summary = "Modify Policy")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Policy Modified",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PolicyModificationResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input provided",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Policy Id Not Found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
            })
      })
  @PostMapping("/modify")
  public ResponseEntity<PolicyResponse> modifyPolicy(
      @Valid @RequestBody PolicyModificationRequest policyModificationRequest) {
    return ResponseEntity.ok(policyFacade.modifyPolicy(policyModificationRequest));
  }

  @Operation(summary = "Fetch Policy")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Policy Fetched",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = PolicyFetchResponse.class))
            }),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input provided",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
            }),
        @ApiResponse(
            responseCode = "404",
            description = "Policy Id Not Found",
            content = {
              @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ErrorResponse.class))
            })
      })
  @PostMapping("/fetch")
  public ResponseEntity<PolicyResponse> fetchPolicy(
      @Valid @RequestBody PolicyFetchRequest policyFetchRequest) {
    return ResponseEntity.ok(policyFacade.fetchPolicy(policyFetchRequest));
  }
}
