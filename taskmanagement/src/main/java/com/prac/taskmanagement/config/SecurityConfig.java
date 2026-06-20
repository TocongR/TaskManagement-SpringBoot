package com.prac.taskmanagement.config;


import com.prac.taskmanagement.security.CustomUserDetailsService;
import com.prac.taskmanagement.security.JwtAuthFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



/*
    Tells Spring:

    "This class contains configuration rules."

    Spring will scan this class
    and create the security setup.
*/
@Configuration



/*
    Enables Spring Security's web security features.

    Basically:

    "Activate security for HTTP requests."
*/
@EnableWebSecurity
public class SecurityConfig {



    /*
        Our custom JWT security guard.

        Every request will pass through this.

        Example:

        GET /api/tasks

        |
        v

        JwtAuthFilter checks token

    */
    private final JwtAuthFilter jwtAuthFilter;



    /*
        Service responsible for loading users.

        Example:

        JWT says:

        username = testuser


        This service asks:

        Database:
        "Give me testusers's information"

    */
    private final CustomUserDetailsService userDetailsService;



    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            CustomUserDetailsService userDetailsService
    ) {

        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;

    }




    /*
        This is the MAIN security configuration.

        Here we define:

        - public routes
        - protected routes
        - JWT behavior
        - security filters

    */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {



        http



                /*
                    Disable CSRF.

                    CSRF is mainly for session-based websites.

                    Example:

                    Browser stores cookies.

                    But JWT APIs usually send token manually:

                    Authorization: Bearer token


                    So we disable it.
                */
                .csrf(csrf -> csrf.disable())




                /*
                    Define which endpoints need authentication.


                    This:

                    /api/auth/**

                    means:


                    /api/auth/login
                    /api/auth/register


                    are PUBLIC.


                    Anyone can access them.

                    Because:

                    User cannot login
                    if login requires login first

                */
                .authorizeHttpRequests(auth -> auth


                        .requestMatchers("/api/auth/**")
                        .permitAll()



                        /*
                            EVERYTHING ELSE:

                            /api/tasks
                            /api/users
                            /api/profile


                            needs a valid JWT.

                        */
                        .anyRequest()
                        .authenticated()

                )




                /*
                    Tell Spring:

                    "Do NOT use server sessions."



                    Traditional login:

                    User logs in
                         |
                         v
                    Server remembers user
                         |
                         v
                    Session ID cookie


                    JWT:

                    User sends token every request.


                    So server does not remember anything.

                */
                .sessionManagement(session ->

                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )

                )




                /*
                    Insert our JWT filter.


                    Spring has many filters:

                    Request
                      |
                      v
                    Security filters
                      |
                      v
                    Controller



                    We say:

                    "Before Spring's normal username/password filter,
                     run our JWT checker."

                */
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );



        return http.build();

    }






    /*
        AuthenticationManager is the thing that performs login checking.


        Example:

        User sends:

        username:
        testuser

        password:
        123456



        AuthenticationManager:

        1. Find user
        2. Compare password hash
        3. Return success/failure


    */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config

    ) throws Exception {


        return config.getAuthenticationManager();

    }

}

/*
                        Request
                          |
                          v
                        SecurityConfig
                          |
                          |
                          +---- Is it /api/auth/register?
                          |          |
                          |          YES
                          |          |
                          |          allow
                          |
                          |
                          NO
                          |
                          v
                        JwtAuthFilter
                          |
                          |
                          +---- Has Bearer token?
                                  |
                                  YES
                                  |
                                  Validate JWT
                                  |
                                  Set SecurityContext
                                  |
                                  v
                              Controller
 */