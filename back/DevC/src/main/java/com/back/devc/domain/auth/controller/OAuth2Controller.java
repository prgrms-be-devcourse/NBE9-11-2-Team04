package com.back.devc.domain.auth.controller;

import com.back.devc.domain.auth.dto.OAuth2MeResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    @GetMapping("/me")
    public OAuth2MeResponse me(@AuthenticationPrincipal OAuth2User oauth2User) {
        if (oauth2User == null) {
            return new OAuth2MeResponse(false, null, List.of(), Map.of());
        }

        List<String> authorities = oauth2User.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new OAuth2MeResponse(
                true,
                oauth2User.getName(),
                authorities,
                oauth2User.getAttributes()
        );
    }
}
