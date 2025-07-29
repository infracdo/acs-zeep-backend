package com.acs_tr069.test_tr069.radius.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acs_tr069.test_tr069.radius.entity.Subscribers;
import com.acs_tr069.test_tr069.radius.repository.SubscriberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api/zeep/")
public class ZeepController {

    @Autowired
    private SubscriberRepository subscriberRepo;

    @GetMapping(path = "/verifyAccount")
  public ResponseEntity<?> validateSubscriber(@RequestParam String username)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (username == null || username.trim().isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
    }
    username = username.trim();
    try {
      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }
      Subscribers subscriber = optionalSubscriber.get();

      Map<String, Object> response = new HashMap<>();
      response.put("id", subscriber.getId());
      response.put("username", subscriber.getUsername());
      response.put("sessionLimit", subscriber.getSessionLimit());
      response.put("remainingSessionTime", subscriber.getRemainingSessionTime());
      response.put("bytesLimit", subscriber.getBytesLimit());
      response.put("remainingBytes", subscriber.getRemainingBytes());
      response.put("status", subscriber.getStatus());

      return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
      String errorMessage = "Failed to validate account. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping(path = "/retrieveAccountList")
  public ResponseEntity<?> fetchAllSubscribers()
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    try {
      List<Subscribers> accounts = subscriberRepo.findAll();
      if (accounts != null && !accounts.isEmpty()) {
        List<Map<String, Object>> responseList = accounts.stream().map(account -> {
          Map<String, Object> response = new HashMap<>();
          response.put("id", account.getId());
          response.put("username", account.getUsername());
          response.put("sessionLimit", account.getSessionLimit());
          response.put("remainingSessionTime", account.getRemainingSessionTime());
          response.put("bytesLimit", account.getBytesLimit());
          response.put("remainingBytes", account.getRemainingBytes());
          response.put("status", account.getStatus());
          response.put("registrationDate", account.getRegistrationDate());
          return response;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(responseList, HttpStatus.OK);
      }
      return new ResponseEntity<>(Collections.emptyList(), HttpStatus.OK);
      
    } catch (Exception e) {
      String errorMessage = "Failed to retrieve accounts. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/registerAccount")
  public ResponseEntity<?> addSubscriber(@RequestBody Subscribers subscriber) {
    try {
      if (subscriber.getUsername() == null || subscriber.getUsername().trim().isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      subscriber.setUsername(subscriber.getUsername().trim());

      if (subscriber.getPassword() == null || subscriber.getPassword().isEmpty()) {
        return new ResponseEntity<>("Password is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      if (subscriber.getSessionLimit() == null || subscriber.getSessionLimit() < 0) {
        subscriber.setSessionLimit(0);
      }

      if (subscriber.getRemainingSessionTime() != null && subscriber.getRemainingSessionTime() < 0) {
        return new ResponseEntity<>("Remaining time is invalid", HttpStatus.BAD_REQUEST);
      }

      if (subscriber.getBytesLimit() != null && subscriber.getBytesLimit() < 0) {
        return new ResponseEntity<>("Bytes limit is invalid", HttpStatus.BAD_REQUEST);
      }

      if (subscriber.getRemainingBytes() != null && subscriber.getRemainingBytes() < 0) {
        return new ResponseEntity<>("Remaining bytes is invalid", HttpStatus.BAD_REQUEST);
      }

      if (subscriber.getStatus() == null) {
        subscriber.setStatus(0);
      }

      if (subscriber.getStatus() != 0 && subscriber.getStatus() != 1) {
        return new ResponseEntity<>("Status is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(subscriber.getUsername());
      if (optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account already exists", HttpStatus.CONFLICT);
      }

      subscriber.setLname("default");
      subscriber.setFname("default");
      subscriber.setAddress("unknown");
      subscriber.setPhoneNo("0000000000");
      subscriber.setBirthdate(LocalDate.now());
      subscriber.setGender("N/A");
      subscriber.setRegistrationDate(LocalDate.now().toString());

      subscriberRepo.save(subscriber);
      return new ResponseEntity<>("Account has been registered", HttpStatus.CREATED);

    } catch (Exception e) {
      String errorMessage = "Failed to register account. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/topupBytes")
  public ResponseEntity<?> addBytes(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null) {
      return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");
      String valueStr = params.get("value");

      if (username == null || username.isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
      }
      username = username.trim();

      if (valueStr == null || valueStr.trim().isEmpty()) {
        return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      int value = 0;
      try {
          value = Integer.parseInt(valueStr.trim());
          if (value <= 0) {
              return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
          }
      } catch (NumberFormatException e) {
          return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      long currentBytesLeft = Optional.ofNullable(subscriber.getRemainingBytes()).orElse(0L);
      long updatedBytesLeft = currentBytesLeft + value;

      subscriber.setRemainingBytes(updatedBytesLeft);
      subscriberRepo.save(subscriber);
      return new ResponseEntity<>("Additional bytes has been added", HttpStatus.OK);

    } catch (Exception e) {
      String errorMessage = "Failed to increase remaining bytes. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/topupTime")
  public ResponseEntity<?> addTime(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null) {
      return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");
      String valueStr = params.get("value");

      if (username == null || username.isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
      }
      username = username.trim();

      if (valueStr == null || valueStr.trim().isEmpty()) {
        return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      int value = 0;

      try {
          value = Integer.parseInt(valueStr.trim());
          if (value <= 0) {
              return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
          }
      } catch (NumberFormatException e) {
          return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      Integer currentTimeLeft = Optional.ofNullable(subscriber.getRemainingSessionTime()).orElse(0);
      int updatedTimeLeft = currentTimeLeft + value;

      subscriber.setRemainingSessionTime(updatedTimeLeft);
      subscriberRepo.save(subscriber);
      return new ResponseEntity<>("Additional time has been added", HttpStatus.OK);

    } catch (Exception e) {
      String errorMessage = "Failed to increase remaining time. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/activateAccount")
  public ResponseEntity<?> enableSubscriber(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null || !params.containsKey("username") || params.get("username").trim().isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      Integer status = subscriber.getStatus();
      if (status == 1) {
        return new ResponseEntity<>("Account is already activated", HttpStatus.BAD_REQUEST);
      }
      subscriber.setStatus(1);
      subscriberRepo.save(subscriber);
      return new ResponseEntity<>("Account has been activated", HttpStatus.OK);

    } catch (Exception e) {
      String errorMessage = "Failed to activate account. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/deactivateAccount")
  public ResponseEntity<?> disableSubscriber(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null || !params.containsKey("username") || params.get("username").trim().isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      Integer status = subscriber.getStatus();
      if (status == 0) {
        return new ResponseEntity<>("Account is already deactivated", HttpStatus.BAD_REQUEST);
      }
      subscriber.setStatus(0);
      subscriberRepo.save(subscriber);
      return new ResponseEntity<>("Account has been deactivated", HttpStatus.OK);

    } catch (Exception e) {
      String errorMessage = "Failed to deactivate account. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/terminateAccount")
  public ResponseEntity<?> deleteSubscriber(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null || !params.containsKey("username") || params.get("username").trim().isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      subscriberRepo.delete(subscriber);
      return new ResponseEntity<>("Account has been terminated", HttpStatus.OK);

    } catch (Exception e) {
      String errorMessage = "Failed to terminate account. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(path = "/changePassword")
  public ResponseEntity<?> changeAccountPassword(@RequestBody Map<String, String> params)
      throws JsonMappingException, JsonProcessingException, InterruptedException {
    if (params == null) {
      return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
    }
    try {
      String username = params.get("username");
      String oldPword = params.get("oldPassword");
      String newPword = params.get("newPassword");

      if (username == null || username.isEmpty()) {
        return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
      }
      
      if (oldPword == null || oldPword.isEmpty()) {
        return new ResponseEntity<>("Password is missing/invalid", HttpStatus.BAD_REQUEST);
      }

      if (newPword == null || newPword.isEmpty()) {
        return new ResponseEntity<>("New password is missing/invalid", HttpStatus.BAD_REQUEST);
      }
      
      if (newPword.length() < 6) {
        return new ResponseEntity<>("New password must be at least 6 characters long", HttpStatus.BAD_REQUEST);
      }

      Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
      if (!optionalSubscriber.isPresent()) {
        return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
      }

      Subscribers subscriber = optionalSubscriber.get();
      String pword = subscriber.getPassword();

      if (pword.equals(oldPword)) {
        subscriber.setPassword(newPword);
        subscriberRepo.save(subscriber);
        return new ResponseEntity<>("Password has been changed", HttpStatus.OK);
      }
        return new ResponseEntity<>("Password change failed", HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      String errorMessage = "Failed to change password. " + e.getMessage();
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
