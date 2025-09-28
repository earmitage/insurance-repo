package com.earmitage.core.security.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.earmitage.core.security.AuthenticationService;
import com.earmitage.core.security.SecurityConfig;
import com.earmitage.core.security.dto.AuthenticationRequest;
import com.earmitage.core.security.dto.GenericResponse;
import com.earmitage.core.security.dto.UserDto;
import com.earmitage.core.security.repository.Image;
import com.earmitage.core.security.repository.ImageRepository;
import com.earmitage.core.security.repository.Subscription;
import com.earmitage.core.security.repository.SubscriptionRepository;
import com.earmitage.core.security.repository.User;
import com.earmitage.core.security.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin
public class AuthenticationCommonRestController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private ImageRepository imageRepository;
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @PostMapping(value = "${app.url}/unsecured/auth/")
    public ResponseEntity<UserDto> createAuthenticationToken(
            @RequestBody final AuthenticationRequest authenticationRequest) {
        final User user = authenticationService.authenticationToken(authenticationRequest.getUsername(),
                authenticationRequest.getPassword());
        //Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameAndSubscriptionExpiryDateAfter(user.getUsername(), LocalDateTime.now());
        UserDto body = new UserDto(user);
        body.setSubscriptions(subscriptions);
        return ResponseEntity.ok(body);

    }

    @PutMapping(value = "${app.url}/secured/users/{username}/")
    public ResponseEntity<UserDto> updateUserProfile(@PathVariable final String username,
            @RequestBody final UserDto updatedUser, @RequestHeader("Authorization") String authHeader) {
        User user = userRepository.findByUsernameAndActiveTrue(username);

        user.setContactType(updatedUser.getContactType());
        user.setDateOfBirth(updatedUser.getDateOfBirth());
        user.setFirstname(updatedUser.getFirstname());
        user.setIdNumber(updatedUser.getIdNumber());
        user.setLastname(updatedUser.getLastname());
        user.setPhone(updatedUser.getPhone());
        user = userRepository.save(user);
        user.setToken(authHeader.substring(7));
        UserDto body = new UserDto(user);
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameAndSubscriptionExpiryDateAfter(user.getUsername(), LocalDateTime.now());
        body.setSubscriptions(subscriptions);
        return ResponseEntity.ok(body);

    }

    @PostMapping("${app.url}/secured/users/{username}/files/")
    public ResponseEntity<List<Image>> uploadFiles(@PathVariable final String username,
            @RequestParam Map<String, String> paramMap, @RequestParam Map<String, MultipartFile> fileMap)
            throws IOException {
        List<Image> savedImages = new ArrayList<>();
        User user = userRepository.findByUsernameAndActiveTrue(username);

        for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
            String fileKey = entry.getKey(); // e.g., file0
            MultipartFile file = entry.getValue();

            String index = fileKey.replace("file", "");
            String typeKey = "type" + index;
            String type = paramMap.get(typeKey);

            if (!file.isEmpty()) {
                Image image = new Image();
                image.setFilename(file.getOriginalFilename());
                image.setType(type);
                image.setContentType(file.getContentType());
                image.setSize(file.getSize());
                image.setData(file.getBytes());
                image.setUser(user);

                savedImages.add(imageRepository.save(image));
            }
        }

        return ResponseEntity.ok(imageRepository.findByUserUsername(username));
    }

    @GetMapping("${app.url}/secured/users/{username}/files/")
    public ResponseEntity<List<Image>> getUploadedFiles(@PathVariable final String username) {
        List<Image> uploadedFiles = imageRepository.findByUserUsername(username);
        return ResponseEntity.ok(uploadedFiles);
    }

    @DeleteMapping("${app.url}/secured/users/{username}/files/{uuid}/")
    public ResponseEntity<List<Image>> deleteFile(@PathVariable final String username,
            @PathVariable final String uuid) {
        Image byUuid = imageRepository.findByUuid(uuid);
        imageRepository.delete(byUuid);
        return ResponseEntity.ok(imageRepository.findByUserUsername(username));
    }

    @GetMapping(value = "${app.url}/secured/refresh/")
    public ResponseEntity<?> refreshAndGetAuthenticationToken(Authentication authentication,
            final HttpServletRequest request) {
        final String token = request.getHeader(securityConfig.getJwtTokenHeader());
        final String username = authentication.getName();
        final User user = (User) userDetailsService.loadUserByUsername(username);

        return ResponseEntity.badRequest().body(null);
    }

    @GetMapping(value = "${app.url}/secured/users/{username}/")
    public ResponseEntity<UserDto> getAuthenticatedUser(@PathVariable final String username, @RequestHeader("Authorization") String authHeader) {
        User user = userRepository.findByUsernameAndActiveTrue(username);
        List<Subscription> subscriptions = subscriptionRepository.findByUserUsernameAndSubscriptionExpiryDateAfter(user.getUsername(), LocalDateTime.now());
        UserDto body = new UserDto(user);
        body.setToken(authHeader.substring(7));
        body.setSubscriptions(subscriptions);
        return ResponseEntity.ok(body);
    }

    @PostMapping("${app.url}/secured/users/{username}/fcm-tokens/")
    @ResponseBody
    public GenericResponse updateFCMToken(@RequestHeader("Authorization") final String authorization,
            @PathVariable("username") final String username, @RequestBody final String token) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFcmToken(token);
            userRepository.save(user);
        });
        return new GenericResponse("success", true);
    }

    @PostMapping("${app.url}/secured/users/unlocked/")
    @ResponseBody
    public GenericResponse unlockUser(@RequestHeader("Authorization") final String authorization,
            @RequestBody final String usernameNameToUnlock) {
        userRepository.findByUsername(usernameNameToUnlock).ifPresent(user -> {
            user.setLocked(false);
            userRepository.save(user);
        });
        return new GenericResponse("success", true);
    }

}
