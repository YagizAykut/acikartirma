package com.acikartirma.acikartirma.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "SecurityFilter", urlPatterns = {"*.xhtml"})
@SuppressWarnings("unused")
public class SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String loginURL = req.getContextPath() + "/login.xhtml";
        String registerURL = req.getContextPath() + "/register.xhtml";
        String indexURL = req.getContextPath() + "/index.xhtml";
        String reqURI = req.getRequestURI();


        boolean isLoggedIn = (session != null && session.getAttribute("valid_user") != null);
        boolean isLoginRequest = reqURI.equals(loginURL);
        boolean isRegisterRequest = reqURI.equals(registerURL);
        boolean isResourceRequest = reqURI.startsWith(req.getContextPath() + "/jakarta.faces.resource");

        if (isLoggedIn) {

            if (isLoginRequest || isRegisterRequest) {
                res.sendRedirect(indexURL);
            } else {
                chain.doFilter(request, response);
            }
        } else if (isLoginRequest || isRegisterRequest || isResourceRequest) {

            chain.doFilter(request, response);
        } else {



            if (isAJAXRequest(req)) {
                res.setContentType("text/xml");
                res.setCharacterEncoding("UTF-8");
                res.getWriter().write("<?xml version='1.0' encoding='UTF-8'?>"
                        + "<partial-response><redirect url='" + loginURL + "'/></partial-response>");
            } else {

                res.sendRedirect(loginURL);
            }
        }
    }


    private boolean isAJAXRequest(HttpServletRequest request) {
        String facesRequest = request.getHeader("Faces-Request");
        return "partial/ajax".equals(facesRequest);
    }
}