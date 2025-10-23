package me.alsesn.backend.service;

import me.alsesn.backend.io.ProfileRequest;
import me.alsesn.backend.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);
}