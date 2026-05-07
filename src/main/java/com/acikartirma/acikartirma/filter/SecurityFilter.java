package com.acikartirma.acikartirma.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebFilter(filterName = "SecurityFilter", urlPatterns = {"*.xhtml"})
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String loginURL = req.getContextPath() + "/login.xhtml";
        String registerURL = req.getContextPath() + "/register.xhtml";
        String reqURI = req.getRequestURI();


        boolean isLoggedIn = (session != null && session.getAttribute("valid_user") != null);


        boolean isLoginRequest = reqURI.equals(loginURL);
        boolean isRegisterRequest = reqURI.equals(registerURL);


        boolean isResourceRequest = reqURI.startsWith(req.getContextPath() + "/jakarta.faces.resource");


        if (isLoggedIn || isLoginRequest || isRegisterRequest || isResourceRequest) {
            chain.doFilter(request, response);
        } else {

            res.sendRedirect(loginURL);
        }
    }
}