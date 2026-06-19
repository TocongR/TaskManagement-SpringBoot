package com.prac.taskmanagement.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;


    public JwtAuthFilter(
            JwtUtil jwtUtil,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }


    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    )
            throws ServletException, IOException {


        /*
            Every HTTP request enters here first.

            Example:

            GET /api/tasks

            Header:
            Authorization: Bearer eyJhbGciOi...

            Our job:
            1. Get token
            2. Check token
            3. Find user
            4. Tell Spring Security:
               "This user is authenticated"
        */


        // Get Authorization header from the request
        String authHeader = request.getHeader("Authorization");


        /*
            If there is no Authorization header
            OR it does not start with "Bearer "

            Example:

            Authorization: Basic something

            We skip JWT checking.

            Why?
            Because maybe this endpoint is public
            like /api/auth/register

        */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {

            // Continue moving the request to the next filter/controller
            filterChain.doFilter(request, response);

            return;
        }



        /*
            Header looks like:

            Authorization: Bearer abcdef12345


            We remove:

            "Bearer "

            leaving only:

            abcdef12345


            This is the actual JWT token.
        */
        String token = authHeader.substring(7);



        /*
            Decode JWT and extract username.

            JWT contains information like:

            {
                "sub": "testuser",
                "iat": "...",
                "exp": "..."
            }


            extractUsername()
            gets the "sub" value.
        */
        String username = jwtUtil.extractUsername(token);



        /*
            Check:

            1. Did we successfully get a username?
            2. Is nobody logged in yet?

            SecurityContextHolder is where Spring stores:
            "Who is currently authenticated?"

            Example:

            SecurityContext:
            {
                user = TestUser
            }


            If user already exists,
            no need to authenticate again.
        */
        if (
                username != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null
        ) {



            /*
                We have username from JWT.

                Now ask database:

                "Give me the user with this username"

                Example:

                JWT says:
                username = testUser


                Database:

                id | username
                1  | testUser


                returns UserDetails object.
            */
            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(username);




            /*
                Now we verify:

                Does this JWT really belong to this user?

                Checks usually:

                - username matches
                - signature is valid
                - token is not expired

            */
            if (jwtUtil.isTokenValid(userDetails, token)) {



                /*
                    Create Spring's authentication object.

                    This tells Spring:

                    "This user is now logged in."


                    Contains:

                    userDetails
                    =
                    who the user is


                    authorities
                    =
                    what roles/permissions they have


                    Example:

                    USER
                    ADMIN

                */
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );




                /*
                    Add extra request information.

                    Example:

                    IP address
                    session details
                    request information

                */
                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );




                /*
                    THE IMPORTANT LINE 🔥


                    We save the authenticated user.

                    From now on:

                    Controller:

                    @GetMapping("/tasks")
                    public List<Task> tasks(
                       Authentication auth
                    )

                    Spring knows who is calling.

                */
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authToken);

            }
        }



        /*
            Continue the request.

            If authentication succeeded:

            Controller receives request as logged-in user.


            If failed:

            Spring Security blocks it.

        */
        filterChain.doFilter(request, response);
    }
}

/*
                    POST /login
                            |
                            v
                    User gives username/password
                            |
                            v
                    Spring checks credentials
                            |
                            v
                    Generate JWT
                            |
                            v
                    Client stores JWT


                    Later:

                    GET /tasks
                    Authorization: Bearer JWT
                            |
                            v
                    JwtAuthFilter
                            |
                            v
                    Extract username
                            |
                            v
                    Find user in database
                            |
                            v
                    Validate token
                            |
                            v
                    SecurityContext = "User is logged in"
                            |
                            v
                    Controller executes
 */