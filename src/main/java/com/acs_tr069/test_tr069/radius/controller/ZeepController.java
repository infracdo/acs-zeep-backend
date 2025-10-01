package com.acs_tr069.test_tr069.radius.controller;

import java.time.LocalDate;
import java.time.OffsetDateTime;
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

import com.acs_tr069.test_tr069.radius.entity.ApAccounting;
import com.acs_tr069.test_tr069.radius.entity.Subscribers;
import com.acs_tr069.test_tr069.radius.entity.SubscribersDTO;
import com.acs_tr069.test_tr069.radius.repository.ApAccountingRepository;
import com.acs_tr069.test_tr069.radius.repository.SubscriberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api/zeep/")
public class ZeepController {

    @Autowired
    private SubscriberRepository subscriberRepo;
    @Autowired
    private ApAccountingRepository apAccountingRepo;

    @GetMapping(path = "/retrieveApData")
    public ResponseEntity<?> fetchApAccountingData(@RequestParam String serialNumber)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        if (serialNumber == null || serialNumber.trim().isEmpty()) {
            return new ResponseEntity<>("Serial number is missing/invalid", HttpStatus.BAD_REQUEST);
        }
        serialNumber = serialNumber.trim();
        try {
            Optional<ApAccounting> optionalDevice = apAccountingRepo.findBySerialNum(serialNumber);
            if (!optionalDevice.isPresent()) {
                return new ResponseEntity<>("Device not found", HttpStatus.NOT_FOUND);
            }
            ApAccounting device = optionalDevice.get();

            Map<String, Object> response = new HashMap<>();
            response.put("serialNumber", device.getSerialNum());
            response.put("calledStationId", device.getCalledStationId());
            response.put("totalInputOctets", device.getTotalInputOctets());
            response.put("totalOutputOctets", device.getTotalOutputOctets());
            response.put("totalSessionTime", device.getTotalSessionTime());
            response.put("provisionedDate", device.getCreatedOn());
            response.put("lastResetDate", device.getLastReset());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String errorMessage = "Failed to retrieve device accounting details. " + e.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/resetApData")
    public ResponseEntity<?> renewApAccountingData(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        if (params == null) {
            return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
        }

        try {
            String serialNumber = params.get("serialNumber");
            if (serialNumber == null || serialNumber.trim().isEmpty()) {
                return new ResponseEntity<>("Serial number is missing/invalid", HttpStatus.BAD_REQUEST);
            }
            serialNumber = serialNumber.trim();

            Optional<ApAccounting> optionalDevice = apAccountingRepo.findBySerialNum(serialNumber);
            if (!optionalDevice.isPresent()) {
                return new ResponseEntity<>("Device not found", HttpStatus.NOT_FOUND);
            }
            OffsetDateTime currenttimetz = OffsetDateTime.now();
            ApAccounting device = optionalDevice.get();

            Map<String, Object> response = new HashMap<>();
            response.put("serialNumber", device.getSerialNum());
            response.put("calledStationId", device.getCalledStationId());
            response.put("totalInputOctets", device.getTotalInputOctets());
            response.put("totalOutputOctets", device.getTotalOutputOctets());
            response.put("totalSessionTime", device.getTotalSessionTime());
            response.put("provisionedDate", device.getCreatedOn());
            response.put("previousResetDate", device.getLastReset() != null ? device.getLastReset() : "Never Reset");
            response.put("currentResetDate", currenttimetz);

            device.setTotalInputOctets(0L);
            device.setTotalOutputOctets(0L);
            device.setTotalSessionTime(0L);
            device.setLastReset(currenttimetz);
            apAccountingRepo.save(device);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("message", "Successfully reset device accounting details");
            finalResponse.put("data", response);

            return ResponseEntity.ok(finalResponse);
        } catch (Exception e) {
            String errorMessage = "Failed to reset device accounting details. " + e.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

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
            response.put("maxUprate", subscriber.getMaxUprate());
            response.put("maxDownrate", subscriber.getMaxDownrate());
            response.put("registrationDate", subscriber.getRegistrationDate());

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
                    response.put("maxUprate", account.getMaxUprate());
                    response.put("maxDownrate", account.getMaxDownrate());
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
    public ResponseEntity<?> addSubscriber(@RequestBody SubscribersDTO subscriberDTO) {
        try {
            subscriberDTO = trimAllStrings(subscriberDTO); // trim given values

            // check if received data is valid
            // deny if username is not valid
            String username = subscriberDTO.getUsername();
            if (username == null || username.isEmpty()) {
                return new ResponseEntity<>("Username is missing", HttpStatus.BAD_REQUEST);
            }
            // deny if password is not valid
            if (subscriberDTO.getPassword() == null || subscriberDTO.getPassword().isEmpty()) {
                return new ResponseEntity<>("Password is missing", HttpStatus.BAD_REQUEST);
            }
            // deny if remaining session time is negative
            if (subscriberDTO.getRemainingSessionTime() != null && subscriberDTO.getRemainingSessionTime() < 0) {
                return new ResponseEntity<>("Remaining time is invalid", HttpStatus.BAD_REQUEST);
            }
            // deny if bytes limit is negative
            if (subscriberDTO.getBytesLimit() != null && subscriberDTO.getBytesLimit() < 0) {
                return new ResponseEntity<>("Bytes limit is invalid", HttpStatus.BAD_REQUEST);
            }
            // deny if remaining bytes is negative
            if (subscriberDTO.getRemainingBytes() != null && subscriberDTO.getRemainingBytes() < 0) {
                return new ResponseEntity<>("Remaining bytes is invalid", HttpStatus.BAD_REQUEST);
            }
            // deny if status is neither 0 nor 1
            if (subscriberDTO.getStatus() != null && subscriberDTO.getStatus() != 0 && subscriberDTO.getStatus() != 1) {
                return new ResponseEntity<>("Status is invalid", HttpStatus.BAD_REQUEST);
            }
            // deny if bytes limit is negative
            if (subscriberDTO.getMaxUprate() != null && subscriberDTO.getMaxUprate() < 0) {
                return new ResponseEntity<>("Maximum uprate is invalid", HttpStatus.BAD_REQUEST);
            }
            // deny if remaining bytes is negative
            if (subscriberDTO.getMaxDownrate() != null && subscriberDTO.getMaxDownrate() < 0) {
                return new ResponseEntity<>("Maximum downrate is invalid", HttpStatus.BAD_REQUEST);
            }

            Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(subscriberDTO.getUsername());
            if (optionalSubscriber.isPresent()) {
                return new ResponseEntity<>("Account already exists", HttpStatus.CONFLICT);
            }

            Subscribers subscriber = convertDtoToEntity(subscriberDTO);
            subscriberRepo.save(subscriber);
            return new ResponseEntity<>("Account has been created", HttpStatus.CREATED);

        } catch (Exception e) {
            String errorMessage = "Failed to register account. " + e.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/setBandwidthLimit")
    public ResponseEntity<?> updateBandwidthLimit(@RequestBody Map<String, String> params)
            throws JsonMappingException, JsonProcessingException, InterruptedException {
        if (params == null) {
            return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
        }
        try {
            String username = params.get("username");
            if (username == null || username.trim().isEmpty()) {
                return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
            }
            username = username.trim();

            String maxuprateStr = params.get("maxUpRate");
            String maxdownrateStr = params.get("maxDownRate");

            Long maxuprate = null;
            Long maxdownrate = null;

            if (maxuprateStr != null && !maxuprateStr.trim().isEmpty()) {
                try {
                    double maxUpRateMb = Double.parseDouble(maxuprateStr.trim());
                    if (maxUpRateMb > 0) {
                        maxuprate = (long) (maxUpRateMb * 1000); // MB to KB conversion
                    }
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }

            if (maxdownrateStr != null && !maxdownrateStr.trim().isEmpty()) {
                try {
                    double maxDownRateMb = Double.parseDouble(maxdownrateStr.trim());
                    if (maxDownRateMb > 0) {
                        maxdownrate = (long) (maxDownRateMb * 1000); // MB to KB conversion
                    }
                } catch (NumberFormatException e) {
                    // do nothing
                }
            }

            if (maxuprate == null && maxdownrate == null) {
                return new ResponseEntity<>("Bandwidth limit values are missing/invalid", HttpStatus.BAD_REQUEST);
            }

            Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
            if (!optionalSubscriber.isPresent()) {
                return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
            }

            Subscribers subscriber = optionalSubscriber.get();

            if (maxuprate != null) {
                subscriber.setMaxUprate(maxuprate);
            }
            if (maxdownrate != null) {
                subscriber.setMaxDownrate(maxdownrate);
            }

            subscriberRepo.save(subscriber);
            return new ResponseEntity<>("Bandwidth limit has been changed", HttpStatus.OK);

        } catch (Exception e) {
            String errorMessage = "Failed to change bandwidth limit. " + e.getMessage();
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/topupBytes")
    public ResponseEntity<?> addBytes(@RequestBody Map<String, String> params)
            throws InterruptedException {
        if (params == null) {
            return new ResponseEntity<>("Invalid input", HttpStatus.BAD_REQUEST);
        }
        String username = params.get("username");
        String valueStr = params.get("value");

        if (username == null || username.isEmpty()) {
            return new ResponseEntity<>("Username is missing/invalid", HttpStatus.BAD_REQUEST);
        }
        username = username.trim();

        if (valueStr == null || valueStr.trim().isEmpty()) {
            return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
        }

        Long valueBytes = null;
        try {
            double valueMb = Double.parseDouble(valueStr.trim());
            if (valueMb <= 0) {
                return ResponseEntity.badRequest().body("Value must be greater than 0");
            }
            valueBytes = (long) (valueMb * 1000000); // Convert MB to bytes
        } catch (NumberFormatException e) {
            return new ResponseEntity<>("Value is missing/invalid", HttpStatus.BAD_REQUEST);
        }

        try {
            Optional<Subscribers> optionalSubscriber = subscriberRepo.findByUsername(username);
            if (!optionalSubscriber.isPresent()) {
                return new ResponseEntity<>("Account not found", HttpStatus.NOT_FOUND);
            }

            Subscribers subscriber = optionalSubscriber.get();
            subscriberRepo.addRemainingBytes(valueBytes, subscriber.getUsername());
            return ResponseEntity.ok("Additional bytes have been credited");

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

            long valueTime = 0;

            try {
                valueTime = Long.parseLong(valueStr.trim());
                if (valueTime <= 0) {
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
            subscriberRepo.addRemainingTime(valueTime, subscriber.getUsername());
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

    private Subscribers convertDtoToEntity(SubscribersDTO dto) {
        Subscribers entity = new Subscribers();
        // set null values to default
        if (dto.getSessionLimit() == null) {
            dto.setSessionLimit(2880000); // set default as 2880000 seconds
        }
        if (dto.getRemainingSessionTime() == null) {
            dto.setRemainingSessionTime(3600); // set default as 3600 seconds
        }
        if (dto.getBytesLimit() == null) {
            dto.setBytesLimit(50000.0); // set default as 50000mb(50gb)
        }
        if (dto.getRemainingBytes() == null) {
            dto.setRemainingBytes(50000.0); // set default as 50000mb(50gb)
        }
        if (isNullOrEmpty(dto.getLname())) {
            dto.setLname("N/A");
        }
        if (isNullOrEmpty(dto.getFname())) {
            dto.setFname("N/A");
        }
        if (isNullOrEmpty(dto.getAddress())) {
            dto.setAddress("N/A");
        }
        if (isNullOrEmpty(dto.getPhoneNo())) {
            dto.setPhoneNo("N/A");
        }
        if (dto.getBirthdate() == null) {
            // dto.setBirthdate(LocalDate.now()); // ALLOW NULL
        }
        if (isNullOrEmpty(dto.getGender())) {
            dto.setGender("N/A");
        }
        if (dto.getStatus() == null) {
            dto.setStatus(0); // set default as disabled
        }
        if (dto.getMaxUprate() == null) {
            dto.setMaxUprate(50.0);
        }
        if (dto.getMaxDownrate() == null) {
            dto.setMaxDownrate(50.0);
        }

        // convert to subscriber entity
        entity.setUsername(dto.getUsername());
        entity.setPassword(dto.getPassword());
        entity.setSessionLimit(dto.getSessionLimit());
        entity.setRemainingSessionTime(dto.getRemainingSessionTime());
        entity.setBytesLimit((long) (dto.getBytesLimit() * 1000000));
        entity.setRemainingBytes((long) (dto.getRemainingBytes() * 1000000));
        entity.setLname(dto.getLname());
        entity.setFname(dto.getFname());
        entity.setMname(dto.getMname());
        entity.setEname(dto.getEname());
        entity.setAddress(dto.getAddress());
        entity.setPhoneNo(dto.getPhoneNo());
        entity.setBirthdate(dto.getBirthdate());
        entity.setGender(dto.getGender());
        entity.setStatus(dto.getStatus());
        entity.setRegistrationDate(LocalDate.now().toString());
        entity.setMaxUprate((long) (dto.getMaxUprate() * 1000));
        entity.setMaxDownrate((long) (dto.getMaxDownrate() * 1000));

        return entity;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private SubscribersDTO trimAllStrings(SubscribersDTO dto) {
        dto.setUsername(safeTrim(dto.getUsername()));
        dto.setLname(safeTrim(dto.getLname()));
        dto.setFname(safeTrim(dto.getFname()));
        dto.setMname(safeTrim(dto.getMname()));
        dto.setEname(safeTrim(dto.getEname()));
        dto.setAddress(safeTrim(dto.getAddress()));
        dto.setPhoneNo(safeTrim(dto.getPhoneNo()));
        dto.setGender(safeTrim(dto.getGender()));
        return dto;
    }

}
