package me.alsesn.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.alsesn.backend.io.ProfileRequest;
import me.alsesn.backend.io.ProfileResponse;
import me.alsesn.backend.service.ProfileService;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ProfileResponse register(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse response = profileService.createProfile(request);
        // TODO: send welcome email

        return response;
    }

    @GetMapping("/profile")
    public ProfileResponse getProfile(
            @CurrentSecurityContext(expression = "authentication?.name") String  email) {
        return profileService.getProfile(email);
    }
}